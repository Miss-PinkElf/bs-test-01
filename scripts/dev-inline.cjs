const { spawn } = require("child_process");
const path = require("path");
const { buildPowerShellArgs, resolveWindowsPowerShell } = require("./powershell-runtime.cjs");

const repoRoot = path.resolve(__dirname, "..");
const isWindows = process.platform === "win32";
const shellExe = isWindows ? resolveWindowsPowerShell() : "bash";
const childProcesses = [];
const streamBuffers = new Map();
let shuttingDown = false;

function flushBufferedLine(label, stream, chunkText) {
  const bufferKey = `${label}:${stream === process.stderr ? "stderr" : "stdout"}`;
  const previous = streamBuffers.get(bufferKey) || "";
  const combined = previous + chunkText;
  const normalized = combined.replace(/\r\n/g, "\n");
  const lines = normalized.split("\n");
  const trailing = lines.pop();

  for (const line of lines) {
    stream.write(`[${label}] ${line}\n`);
  }

  streamBuffers.set(bufferKey, trailing || "");
}

function flushRemainingBuffers() {
  for (const [key, value] of streamBuffers.entries()) {
    if (!value) {
      continue;
    }

    const [label, streamName] = key.split(":");
    const stream = streamName === "stderr" ? process.stderr : process.stdout;
    stream.write(`[${label}] ${value}\n`);
    streamBuffers.set(key, "");
  }
}

function killProcessTree(child) {
  if (!child || child.killed) {
    return;
  }

  if (isWindows) {
    const killer = spawn("taskkill", ["/pid", String(child.pid), "/t", "/f"], {
      stdio: "ignore"
    });

    killer.on("error", () => {});
    return;
  }

  child.kill("SIGTERM");
}

function shutdown(exitCode) {
  if (shuttingDown) {
    return;
  }

  shuttingDown = true;

  for (const child of childProcesses) {
    killProcessTree(child);
  }

  setTimeout(() => {
    flushRemainingBuffers();
    process.exit(exitCode);
  }, 300);
}

function buildProcessArgs(scriptPath) {
  if (!isWindows) {
    return [scriptPath];
  }

  return buildPowerShellArgs(scriptPath);
}

function startProcess(label, scriptName) {
  const scriptPath = path.join(repoRoot, "scripts", scriptName);
  const args = buildProcessArgs(scriptPath);

  const child = spawn(shellExe, args, {
    cwd: repoRoot,
    env: process.env,
    stdio: ["ignore", "pipe", "pipe"]
  });

  childProcesses.push(child);

  child.stdout.on("data", (chunk) => {
    flushBufferedLine(label, process.stdout, chunk.toString("utf8"));
  });

  child.stderr.on("data", (chunk) => {
    flushBufferedLine(label, process.stderr, chunk.toString("utf8"));
  });

  child.on("exit", (code) => {
    if (shuttingDown) {
      return;
    }

    flushRemainingBuffers();

    if (code && code !== 0) {
      process.stderr.write(`[${label}] exited with code ${code}\n`);
      shutdown(code);
      return;
    }

    process.stdout.write(`[${label}] exited.\n`);
    shutdown(0);
  });

  child.on("error", (error) => {
    if (shuttingDown) {
      return;
    }

    process.stderr.write(`[${label}] failed to start: ${error.message}\n`);
    shutdown(1);
  });
}

process.on("SIGINT", () => {
  shutdown(0);
});

process.on("SIGTERM", () => {
  shutdown(0);
});

startProcess("backend", "start-backend.ps1");
startProcess("frontend", "start-frontend.ps1");

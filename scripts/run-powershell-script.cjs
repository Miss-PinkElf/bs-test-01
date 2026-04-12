const { spawn } = require("child_process");
const { buildPowerShellArgs, resolveWindowsPowerShell } = require("./powershell-runtime.cjs");

if (process.platform !== "win32") {
  console.error("run-powershell-script.cjs only supports Windows.");
  process.exit(1);
}

const [, , scriptPath, ...scriptArgs] = process.argv;

if (!scriptPath) {
  console.error("Usage: node ./scripts/run-powershell-script.cjs <script.ps1> [args...]");
  process.exit(1);
}

const shellExe = resolveWindowsPowerShell();
const child = spawn(shellExe, buildPowerShellArgs(scriptPath, scriptArgs), {
  cwd: process.cwd(),
  env: process.env,
  stdio: "inherit",
  windowsHide: false
});

child.on("exit", (code) => {
  process.exit(code ?? 0);
});

child.on("error", (error) => {
  console.error(`Failed to start ${shellExe}: ${error.message}`);
  process.exit(1);
});

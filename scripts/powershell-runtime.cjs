const { spawnSync } = require("child_process");
const path = require("path");

function resolveWindowsPowerShell() {
  if (process.platform !== "win32") {
    return null;
  }

  for (const candidate of ["pwsh", "powershell"]) {
    const result = spawnSync(candidate, ["-NoProfile", "-Command", "$PSVersionTable.PSVersion.ToString()"], {
      stdio: "ignore",
      windowsHide: true
    });

    if (!result.error && result.status === 0) {
      return candidate;
    }
  }

  return "powershell";
}

function buildPowerShellArgs(scriptPath, extraArgs = []) {
  return [
    "-NoProfile",
    "-ExecutionPolicy",
    "Bypass",
    "-File",
    path.resolve(scriptPath),
    ...extraArgs
  ];
}

module.exports = {
  buildPowerShellArgs,
  resolveWindowsPowerShell
};

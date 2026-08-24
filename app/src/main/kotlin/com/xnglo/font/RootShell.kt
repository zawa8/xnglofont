package com.xnglo.font

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Minimal su-shell runner. No third-party root library -- just talks
 * to the `su` binary directly, which is what Magisk (and every other
 * root solution) provides on PATH once the user grants root to this
 * app.
 */
object RootShell {

    data class ShellResult(val exitCode: Int, val stdout: String, val stderr: String)

    /** True if a `su` binary exists and a trivial command run as root succeeds. */
    fun hasRoot(): Boolean {
        return try {
            val result = run("id")
            result.exitCode == 0 && result.stdout.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Runs one or more shell commands as root in a single `su` session.
     * Each command is a separate line; failures in one command don't
     * stop the rest (matches normal shell script behavior unless you
     * add `set -e` yourself).
     */
    fun run(vararg commands: String): ShellResult {
        val process = ProcessBuilder("su").redirectErrorStream(false).start()
        val stdin = DataOutputStream(process.outputStream)
        for (cmd in commands) {
            stdin.writeBytes(cmd + "\n")
        }
        stdin.writeBytes("exit\n")
        stdin.flush()
        stdin.close()

        val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
        val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
        val exitCode = process.waitFor()
        return ShellResult(exitCode, stdout, stderr)
    }
}

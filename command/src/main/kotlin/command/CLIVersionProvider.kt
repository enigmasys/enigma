package command

import command.utils.Utils
import picocli.CommandLine

class CLIVersionProvider: CommandLine.IVersionProvider   {
    override fun getVersion(): Array<String> {
        return Utils.getCurrentAndReleaseVersion()
    }
}

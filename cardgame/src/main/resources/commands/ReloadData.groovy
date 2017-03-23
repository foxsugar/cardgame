package commands

import com.code.server.cardgame.CardgameApplication
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext

/**
 * Created by sunxianping on 2017/3/20.
 */
class ReloadData {
    @Usage("reloadData")
    @Command
    def main(InvocationContext context) {
        CardgameApplication.init()
        return "reload success!"
    }


    @Usage("re")
    @Command
    def re(InvocationContext context) {
        CardgameApplication.init()
        return "reload success!"
    }
}

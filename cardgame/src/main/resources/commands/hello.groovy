package commands

import com.code.server.cardgame.utils.IdWorker
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext

/**
 * Created by sunxianping on 2017/3/17.
 */
class hello {
    @Usage("Say Hello")
    @Command
    def main(InvocationContext context) {
        return "Hello"
    }
}

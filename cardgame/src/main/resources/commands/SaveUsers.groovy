package commands

import com.code.server.cardgame.utils.DbUtils
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext

/**
 * Created by sunxianping on 2017/3/17.
 */
class SaveUsers {
    @Usage("SaveUsers")
    @Command
    def main(InvocationContext context) {
        DbUtils.saveUsers();
        return "save success!"
    }
}

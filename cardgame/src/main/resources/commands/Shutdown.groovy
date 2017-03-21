package commands

import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext

/**
 * Created by sunxianping on 2017/3/17.
 */
class Shutdown {
    @Usage("shutdown")
    @Command
    def main(InvocationContext context) {

        Shutdown.
        return "shutdown"
    }

    public def test(){
        println "================================================"
    }
}

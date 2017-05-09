package commands

import com.code.server.cardgame.core.GameManager
import com.code.server.cardgame.handler.GameProcessor
import com.code.server.cardgame.utils.IdWorker
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext

/**
 * Created by sunxianping on 2017/3/17.
 */
class show {
    @Usage("show")
    @Command
    def main(InvocationContext context) {
        String s = ""
        s+="gamemanager容器数量 : "
        s+=GameManager.getInstance().toString()
        s+="\n消息队列数量 : " +  GameProcessor.getInstance().messageQueue.size()
        return s
    }
}

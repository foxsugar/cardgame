package commands

import com.code.server.cardgame.core.GameManager
import com.code.server.cardgame.core.Player
import com.code.server.cardgame.utils.DbUtils
import com.code.server.cardgame.utils.SpringUtil
import com.code.server.db.Service.UserService
import com.code.server.db.model.User
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


    @Usage("saveAll")
    @Command
    def saveAll(InvocationContext context) {
        UserService userService = SpringUtil.getBean(UserService.class)
        List<User> users = new ArrayList<>()

        for (Player player : GameManager.getInstance().players.values()) {
            User user = player.getUser();

            user.column1 = 11
            users.add(user)

        }
        println(users.size())
        userService.batchUpdate(users);
//        userService.userDao.save(users)
        return "save success!"
    }
}

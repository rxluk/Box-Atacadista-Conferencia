package box.atacadista.controller;

import box.atacadista.dao.UserDao;
import box.atacadista.model.User;
import box.atacadista.model.UserRole;

public class UserController {

    private UserDao userDao = new UserDao();

    public boolean authenticate(String login, String password) {
        User user = userDao.authenticate(login, password);
        return user != null;
    }

    public void dbInicializer() {
        userDao.insertUser(new User(null, "administrador", "admin", "123", UserRole.ADMIN));
    }
}

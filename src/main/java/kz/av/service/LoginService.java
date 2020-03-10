package kz.av.service;

import kz.av.dao.DaoException;
import kz.av.dao.UserDao;
import kz.av.entity.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

@Service
@ComponentScan
public class LoginService {
    private static final Logger logger = Logger.getRootLogger();

    @Autowired
    private UserDao userDao;

    public LoginService(UserDao userDao) {
        this.userDao = userDao;
    }

    public LoginService() {
    }

    public User getUser(User model) {
        User user = null;

        try {
            user = userDao.getByUsername(model.getUsername());
        } catch (DaoException e) {
            e.printStackTrace();
        }

        return user;
    }


}
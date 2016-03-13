package carldav.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.service.UserService;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Kamill Sokol
 */
@RequestMapping("user")
@RestController
public class UserController {

    private final UserService userService;
    private final UserDao userDao;

    @Autowired
    public UserController(final UserService userService, final UserDao userDao) {
        Assert.notNull(userDao, "userDao is null");
        Assert.notNull(userService, "userService is null");
        this.userDao = userDao;
        this.userService = userService;
    }

    @RequestMapping(method = GET)
    public Set<String> get() {
        final Set<String> users = new TreeSet<>();
        for (final User user : userDao.findAll()) {
            users.add(user.getEmail());
        }
        return users;
    }

    @ResponseStatus(CREATED)
    @RequestMapping(method = POST)
    public void post(@RequestBody UserCreateRequest request) {
        final User user = new User();

        user.setEmail(request.email);
        user.setPassword(request.password);

        userService.createUser(user);
    }

    static class UserCreateRequest {
        String email;
        String password;

        public void setEmail(final String email) {
            this.email = email;
        }

        public void setPassword(final String password) {
            this.password = password;
        }
    }
}

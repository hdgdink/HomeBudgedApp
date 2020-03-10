package kz.av.controller;


import kz.av.entity.User;
import kz.av.service.LoginService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Locale;

@Controller
@RequestMapping("/")
public class MainController {
    private final static String USER_ATT = "user";
    private final static String ERROR_ATT = "error";
    private Logger logger = Logger.getRootLogger();

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private LoginService loginService;

    @RequestMapping()
    public String start(HttpSession session, Model model) {
        Locale locale = (Locale) session.getAttribute("locale");
        if (locale == null) {
            session.setAttribute("locale", Locale.getDefault());
        }
        model.addAttribute(USER_ATT, new User());

        return "index";
    }


    @RequestMapping(value = "/locale")
    public String changeLocale(HttpSession session, Locale locale) {
        session.setAttribute("locale", locale);
        String referer = request.getHeader("referer");

        return "redirect:/" + referer.substring(referer.lastIndexOf("/") + 1, referer.length());
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView login(@ModelAttribute User userModel, HttpSession session) {
        User loggedUser = loginService.getUser(userModel);
        logger.info(loggedUser.toString());

        if (loggedUser.getUsername() == null) {
            logger.debug("Wrong username");
            return setModel(ERROR_ATT, "account.notFound", "index");
        }

        if (!loggedUser.getPassword().equals(userModel.getPassword())) {
            logger.debug("Wrong password");
            return setModel(ERROR_ATT, "account.isBad", "index");
        }

        return setLoggedModel(session, loggedUser);
    }

    @RequestMapping(value = "/calendar")
    public String getCalendar(){
        return "calendar";
    }

    private ModelAndView setLoggedModel(HttpSession session, User user) {
        ModelAndView modelAndView = new ModelAndView();
        session.setAttribute(USER_ATT, user);
        modelAndView.setViewName("redirect:/calendar");
        modelAndView.addObject(USER_ATT, user);
        logger.info("Success login");
        return modelAndView;
    }


    private ModelAndView setModel(String attName, Object attVal, String view) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(attName, attVal);
        modelAndView.setViewName(view);
        return modelAndView;
    }

    private void populateError(String field, BindingResult bindingResult, ModelAndView modelAndView) {
        modelAndView.setViewName("index");

        if (bindingResult.hasFieldErrors(field)) {
            modelAndView.addObject("error", bindingResult.getFieldError(field)
                    .getDefaultMessage());
        }
    }
}


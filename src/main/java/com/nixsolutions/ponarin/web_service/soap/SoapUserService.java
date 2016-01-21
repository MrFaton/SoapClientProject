package com.nixsolutions.ponarin.web_service.soap;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.nixsolutions.ponarin.entity.User;

@WebService
public interface SoapUserService {

    @WebMethod
    void create(User user);

    @WebMethod
    void update(User user);

    @WebMethod
    void remove(User user);

    @WebMethod
    List<User> findAll();

    @WebMethod
    User findById(long id);

    @WebMethod
    User findByLogin(String login);

    @WebMethod
    User findByEmail(String email);

}

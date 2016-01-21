package com.nixsolutions.ponarin.web_service.soap;

import java.net.URL;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.nixsolutions.ponarin.entity.Role;
import com.nixsolutions.ponarin.entity.User;

public class SoapUserServiceTest {
    private static final String WSDL_URL = "http://10.10.34.83:8080/WebServiceProject/soap_user_service?wsdl";
    private static final String NAMESPACE_URI = "http://impl.soap.web_service.ponarin.nixsolutions.com/";
    private static final String LOCAL_PART = "SoapUserServiceImplService";

    private static SoapUserService soapUserService;

    private User testUser;

    @BeforeClass
    public static void generalSetUp() throws Exception {
        URL url = new URL(WSDL_URL);
        QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
        Service service = Service.create(url, qName);
        soapUserService = service.getPort(SoapUserService.class);
    }

    @Before
    public void setUp() {
        testUser = getDefaultUser();
        soapUserService.create(testUser);
        User actualUser = soapUserService.findByLogin(testUser.getLogin());

        if (actualUser == null) {
            throw new RuntimeException("User can't be null");
        }

        testUser.setId(actualUser.getId());
    }

    @After
    public void tearDown() {
        soapUserService.remove(testUser);
    }

    @Test
    public void testCreate() {
        tearDown();
        testUser = getDefaultUser();

        soapUserService.create(testUser);

        User actualUser = soapUserService.findByLogin(testUser.getLogin());

        assertNotNull("User must exists in db", actualUser);
        testUser.setId(actualUser.getId());
        assertEquals("Users must equals", testUser, actualUser);
    }

    @Test
    public void testCreateLoginDublicate() {
        testUser.setEmail("new_email@mail.ru");
        try {
            soapUserService.create(testUser);
            fail("Must be thrown exception couse login is dublicate");
        } catch (Exception ex) {
        }
    }

    @Test
    public void testCreateWithEmailDublicate() {
        testUser.setLogin("newTestLogin");
        try {
            soapUserService.create(testUser);
            fail("Must be thrown exception couse email is dublicate");
        } catch (Exception ex) {
        }
    }
    
    @Test
    public void testUpdate() {
        testUser.setPassword(testUser.getPassword() + "12erph6");
        testUser.setEmail("new_test_email@mail.ru");
        testUser.setFirstName("updated first name");
        testUser.setLastName("updated last name");
        testUser.setBirthDay(new Date());

        Role role = new Role();
        role.setId(2);
        role.setName("User");
        testUser.setRole(role);
        
        soapUserService.update(testUser);
        
        User extractedUser = soapUserService.findByLogin(testUser.getLogin());
    }

    private User getDefaultUser() {
        User user = new User();

        user.setLogin("testLogin");
        user.setPassword("Q!q1q1");
        user.setEmail("testEmail@mail.ru");
        user.setFirstName("First Name");
        user.setLastName("Last Name");
        user.setBirthDay(new Date());

        Role role = new Role();
        role.setId(1);
        role.setName("Admin");
        user.setRole(role);

        return user;
    }
}

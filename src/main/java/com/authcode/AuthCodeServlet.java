package com.authcode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * User: Dori Ding
 * Date: 13-1-10
 * Time: 12:06
 */
public class AuthCodeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cookie = req.getParameter("cookie1");
        resp.setContentType("application/json");
        System.out.println("========" + cookie);

        PrintWriter writer = resp.getWriter();
        writer.print("{test:1234}");
        writer.flush();
        writer.close();
    }
}

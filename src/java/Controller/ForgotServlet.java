/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package Controller;

import DAO.UserDAO;
import Service.AuthorizationService;
import Service.MailService;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author ADMIN
 */
public class ForgotServlet extends HttpServlet {


    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (!AuthorizationService.gI().Authorization(request, response)) {
                return;
            }
        } catch(Exception e) {}
        request.getRequestDispatcher("forgotPassword.jsp").forward(request, response);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (!AuthorizationService.gI().Authorization(request, response)) {
                return;
            }
        } catch(Exception e) {}
             
        String password = request.getParameter("password");
        if (password != null) {
            String repass = request.getParameter("repass");
            HttpSession ses = request.getSession();
            String email = (String) ses.getAttribute("email");
            if (repass.equals(password)) {
                try {
                    boolean b = UserDAO.changePassword(email, password);
                    if (!b) {
                        request.setAttribute("message", "You cannot use your old password!");
                        request.getRequestDispatcher("newPass.jsp").forward(request, response);
                        return;
                    }
                } catch (Exception e) {
                }
                request.getSession().setAttribute("error", "Change password successfully!");
                response.sendRedirect("signin");
                return;
            } else {
                request.setAttribute("message", "Wrong password confirmation, please enter again!");
                request.getRequestDispatcher("newPass.jsp").forward(request, response);
                return;
            }
        } else {
            String value = request.getParameter("recode");
            if (value == null) { //Send Code
                String email = request.getParameter("email");
                String username = request.getParameter("username");
                try {
                    if (UserDAO.isRegistered(email, username)) {
                        Random ran = new Random();
                        int num = ran.nextInt(999999);
                        String otp = String.format("%06d", num);
                        final String tempMail = email;
                        final String tempOTP = otp;
                        Thread thread = new Thread() {
                            public void run() {
                                try {
                                    MailService.sendMail(tempMail, "Recover Password", "Here is your recovery code: " + tempOTP);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                        HttpSession ses = request.getSession();
                        request.setAttribute("message", "Recovery code has been sent to your email!");
                        ses.setAttribute("otp", otp);
                        ses.setAttribute("email", email);
                        request.getRequestDispatcher("recoveryCodeForgotPass.jsp").forward(request, response);
                        return;
                    } else {
                        request.setAttribute("message", "Email or username has not been registed!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else { //Confirm Code
                HttpSession ses = request.getSession();
                String otp = (String) ses.getAttribute("otp");

                if (value.equals(otp)) {
                    ses.removeAttribute("otp");
                    request.getRequestDispatcher("newPass.jsp").forward(request, response);
                } else {
                    request.setAttribute("message", "Wrong code, please try again.");
                    request.getRequestDispatcher("recoveryCode.jsp").forward(request, response);
                }
                return;
            }
        }
        request.getRequestDispatcher("forgotPassword.jsp").forward(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

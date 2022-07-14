<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Versamento Sincrono</title>
        <style>
            h1 {
                font: 25px verdana,verdana, arial;
                margin: 20px 0px 40px 10px;
                padding: 0;
                border-collapse: collapse;
                text-align: left;
                color: #333;
                line-height: 19px;
            }
            h2 {
                font: 17px verdana,verdana, arial;
                margin: 20px 0px 40px 10px;
                padding: 0;
                border-collapse: collapse;
                text-align: left;
                color: #333;
                line-height: 19px;
            }
            .green{
                background: green;
                padding:0px 6px;
                border:1px solid #3b6e22;
                height:24px;
                line-height:24px;
                color:#FFFFFF;
                font-size:12px;
                margin:20px 10px 0 20px;
                display:inline-block;
                text-decoration:none;
            }
            div.box .input-text{
                border:1px solid #3b6e22;
                color:#666666;
            }

            div.box label{
                display:block;
                margin-bottom:10px;
                color:#555555;
            }

            div.box label span{
                font: 11px verdana,verdana, arial;
                display:block;
                float:left;
                padding-right:6px;
                width:150px;
                text-align:left;
                font-weight:bold;
            }
            table {
                font: 11px verdana,verdana, arial;
                margin: 0;
                padding: 0;
                border-collapse: collapse;
                text-align: left;
                color: #333;
                line-height: 19px;
            }

            caption {
                font-size: 14px;
                font-weight: bold;
                margin-bottom: 20px;
                text-align: left;
                text-transform: uppercase;
            }

            td {
                margin: 0;
                padding: 10px 10px;
                border: 1px dotted #f5f5f5;
            }

            th {
                font-weight: normal;
                text-transform: uppercase;
            }

            thead tr th {
                background-color: #575757;
                padding:  20px 10px;
                color: #fff;
                font-weight: bold;
                border-right: 2px solid #333;
                text-transform: uppercase;
                text-align:center;
            }

            tfoot tr th, tfoot tr td {
                background-color: transparent;
                padding:  20px 10px;
                color: #ccc;
                border-top: 1px solid #ccc;
            }

            tbody tr th {
                padding: 10px 10px;
                border-bottom: 1px dotted #fafafa;
            }

            tr {
                background-color: #FBFDF6;
            }
            tr.odd {
                background-color: #EDF7DC;
            }

            tr:hover {
            }

            tr:hover td, tr:hover td a, tr:hover th a {
                color: #a10000;
            }

            td:hover {
            }

            tr:hover th a:hover {
                background-color: #F7FBEF;
                border-bottom: 2px solid #86C200;
            }

            table a {
                color: #608117;
                background-image: none;
                text-decoration: none;
                border-bottom: 1px dotted #8A8F95;
                padding: 2px;
                padding-right: 12px;
            }

            table a:hover {
                color: #BBC4CD;
                background-image: none;
                text-decoration: none;
                border-bottom: 3px solid #333;
                padding: 2px;
                padding-right: 12px; color: #A2A2A2;
            }

            table a:visited {
                text-decoration: none;
                border-bottom: 1px dotted #333;
                text-decoration: none;
                padding-right: 12px; color: #A2A2A2;
            }

            table a:visited:hover {
                background-image: none;
                text-decoration: none;
                border-bottom: 3px solid #333;
                padding: 2px;
                padding-right: 12px; color: #A2A2A2;
            }

        </style>
    </head>
    <body>
        <h1>Versamento Multimedia</h1>
        <h2>Viene chiamata la servlet/ws <b>VersamentoMultiMedia</b></h2>
        <form action="VersamentoMultiMedia" method="post"  enctype="multipart/form-data" >
            <div class="box">
                <label>
                    <span>VERSIONE</span>
                    <input type="text" size="10" name="VERSIONE" class="input-text" value="1.3"></input>
                </label>

                <label>
                    <span>LOGINNAME</span>
                    <input type="text" size="100" name="LOGINNAME" class="input-text"></input>
                </label>

                <label>
                    <span>PASSWORD</span>
                    <input type="text" size="100" name="PASSWORD" class="input-text"></input>
                </label>
                <label>
                    <span>XMLINDICE</span>
                    <TEXTAREA NAME="XMLINDICE" ROWS=20, COLS=100 class="input-text"></TEXTAREA>
                </label>
                <label>
                    <span>XMLSIP</span>
                    <TEXTAREA NAME="XMLSIP" ROWS=30, COLS=100 class="input-text"></TEXTAREA>
                </label>

                <input class="green"  type="submit" value="Invio"></input>
            </div>
        </form>

    </body>
</html>
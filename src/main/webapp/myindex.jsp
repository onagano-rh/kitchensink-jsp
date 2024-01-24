<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="ISO-8859-1" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="ja">

<head>
    <title>kitchensink-jsp</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link rel="stylesheet" type="text/css" href="https://csszengarden.com/219/219.css" />
</head>

<body>
    <h1>Members</h1>
    <table border="1" style="margin-right:auto; margin-left:auto;"">
        <thead>
            <tr>
                <th>Name</th>
                <th>Email</th>
                <th>PhoneNumber</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${myList}" var="member">
                <tr>
                    <td>
                        <c:out value="${member.name}" />
                    </td>
                    <td>
                        <c:out value="${member.email}" />
                    </td>
                    <td>
                        <c:out value="${member.phoneNumber}" />
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</body>

</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Города и парки</title>
    <link rel="stylesheet" href="<c:url value='/css/bootstrap.min.css' />">
    <link rel="stylesheet" href="<c:url value='/css/style.css' />">
    <script defer src="<c:url value='/js/bootstrap.min.js' />"></script>
</head>
<body>
    <jsp:include page="/views/header.jsp" />
    <main class="container">
        <h1 class="page-title">Главное меню</h1>
        <ul class="list-group list-group-flush">
            <li class="list-group-item"><a href="<c:url value='/city' />">Города</a></li>
            <li class="list-group-item"><a href="<c:url value='/park' />">Парки</a></li>
        </ul>
    </main>
    <jsp:include page="/views/footer.jsp" />
</body>
</html>

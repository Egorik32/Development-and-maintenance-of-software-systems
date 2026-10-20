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
    <script defer src="<c:url value='/js/jquery-3.6.4.js' />"></script>
    <script defer src="<c:url value='/js/bootstrap.bundle.min.js' />"></script>
    <script defer src="<c:url value='/js/bootstrap-check.js' />"></script>
</head>
<body>
    <jsp:include page="/views/header.jsp" />
    <main class="container">
        <h1 class="page-title">Главное меню</h1>
        <ul class="list-group list-group-flush">
            <li class="list-group-item"><a href="<c:url value='#' />">Города</a></li>
            <li class="list-group-item"><a href="<c:url value='#' />">Парки</a></li>
        </ul>
        <button id="bootstrap-check" type="button" class="btn btn-primary mt-3"
                data-bs-toggle="popover" data-bs-trigger="hover focus"
                data-bs-title="Сообщение" data-bs-content="Ура, Bootstrap 5 работает">
            Поднеси ко мне курсор
        </button>
    </main>
    <jsp:include page="/views/footer.jsp" />
</body>
</html>

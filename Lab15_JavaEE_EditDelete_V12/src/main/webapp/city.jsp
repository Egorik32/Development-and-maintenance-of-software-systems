<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Города</title>
    <link rel="stylesheet" href="<c:url value='/css/bootstrap.min.css' />">
    <link rel="stylesheet" href="<c:url value='/css/style.css' />">
    <script defer src="<c:url value='/js/bootstrap.min.js' />"></script>
</head>
<body>
    <jsp:include page="/views/header.jsp" />
    <main class="container-fluid">
        <h1 class="page-title">Города</h1>
        <div class="row g-3">
            <section class="col-lg-8">
                <div>
                    <h2 class="h4">Список городов</h2>
                    <table class="table table-striped align-middle">
                        <thead>
                            <tr>
                                <th>Код</th>
                                <th>Название</th>
                                <th>Статус</th>
                                <th>Население</th>
                                <th>Парк</th>
                                <th>Действия</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="city" items="${cities}">
                                <tr>
                                    <td>${city.id}</td>
                                    <td>${city.name}</td>
                                    <td>${city.status}</td>
                                    <td>${city.population}</td>
                                    <td>${city.park.name}</td>
                                    <td class="table-actions">
                                        <a class="btn btn-sm btn-outline-primary" href="<c:url value='/editcity?id=${city.id}' />">Редактировать</a>
                                        <a class="btn btn-sm btn-outline-danger" onclick="return confirm('Удалить запись?')" href="<c:url value='/deletecity?id=${city.id}' />">Удалить</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>
            <section class="col-lg-4">
                <div>
                    <h2 class="h4">Новый город</h2>
                    <form method="post" action="">
                        <div class="mb-3">
                            <label class="form-label" for="name">Название</label>
                            <input class="form-control" type="text" id="name" name="name" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="status">Статус</label>
                            <input class="form-control" type="text" id="status" name="status" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="population">Население</label>
                            <input class="form-control" type="number" min="0" id="population" name="population" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="parkId">Парк</label>
                            <select class="form-select" id="parkId" name="parkId" required>
                                <c:forEach var="park" items="${parks}">
                                    <option value="${park.id}">${park.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <button class="btn btn-primary" type="submit">Добавить</button>
                    </form>
                </div>
            </section>
        </div>
    </main>
    <jsp:include page="/views/footer.jsp" />
</body>
</html>

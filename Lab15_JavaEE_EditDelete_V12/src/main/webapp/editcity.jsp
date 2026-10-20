<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Редактирование города</title>
    <link rel="stylesheet" href="<c:url value='/css/bootstrap.min.css' />">
    <link rel="stylesheet" href="<c:url value='/css/style.css' />">
    <script defer src="<c:url value='/js/bootstrap.min.js' />"></script>
</head>
<body>
    <jsp:include page="/views/header.jsp" />
    <main class="container-fluid">
        <h1 class="page-title">Редактирование города</h1>
        <div class="row g-3">
            <section class="col-lg-7">
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
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>
            <section class="col-lg-5">
                <div>
                    <h2 class="h4">Данные города</h2>
                    <form method="post" action="">
                        <input type="hidden" name="id" value="${cityEdit.id}">
                        <div class="mb-3">
                            <label class="form-label" for="readonlyId">Код</label>
                            <input class="form-control" type="text" id="readonlyId" value="${cityEdit.id}" readonly>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="name">Название</label>
                            <input class="form-control" type="text" id="name" name="name" value="${cityEdit.name}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="status">Статус</label>
                            <input class="form-control" type="text" id="status" name="status" value="${cityEdit.status}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="population">Население</label>
                            <input class="form-control" type="number" min="0" id="population" name="population" value="${cityEdit.population}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="parkId">Парк</label>
                            <select class="form-select" id="parkId" name="parkId" required>
                                <c:forEach var="park" items="${parks}">
                                    <option value="${park.id}" ${park.id == cityEdit.park.id ? 'selected' : ''}>${park.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <button class="btn btn-primary" type="submit">Сохранить</button>
                        <a class="btn btn-secondary" href="<c:url value='/city' />">Отменить</a>
                    </form>
                </div>
            </section>
        </div>
    </main>
    <jsp:include page="/views/footer.jsp" />
</body>
</html>

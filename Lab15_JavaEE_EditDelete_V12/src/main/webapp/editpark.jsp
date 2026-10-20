<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Редактирование парка</title>
    <link rel="stylesheet" href="<c:url value='/css/bootstrap.min.css' />">
    <link rel="stylesheet" href="<c:url value='/css/style.css' />">
    <script defer src="<c:url value='/js/bootstrap.min.js' />"></script>
</head>
<body>
    <jsp:include page="/views/header.jsp" />
    <main class="container-fluid">
        <h1 class="page-title">Редактирование парка</h1>
        <div class="row g-3">
            <section class="col-lg-7">
                <div>
                    <h2 class="h4">Список парков</h2>
                    <table class="table table-striped align-middle">
                        <thead>
                            <tr>
                                <th>Код</th>
                                <th>Название</th>
                                <th>Тип</th>
                                <th>Площадь</th>
                                <th>Водоем</th>
                                <th>Дата постройки</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="park" items="${parks}">
                                <tr>
                                    <td>${park.id}</td>
                                    <td>${park.name}</td>
                                    <td>${park.type}</td>
                                    <td>${park.area}</td>
                                    <td>${park.hasWater ? 'есть' : 'нет'}</td>
                                    <td>${park.buildDate}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>
            <section class="col-lg-5">
                <div>
                    <h2 class="h4">Данные парка</h2>
                    <form method="post" action="">
                        <input type="hidden" name="id" value="${parkEdit.id}">
                        <div class="mb-3">
                            <label class="form-label" for="readonlyId">Код</label>
                            <input class="form-control" type="text" id="readonlyId" value="${parkEdit.id}" readonly>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="name">Название</label>
                            <input class="form-control" type="text" id="name" name="name" value="${parkEdit.name}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="type">Тип</label>
                            <input class="form-control" type="text" id="type" name="type" value="${parkEdit.type}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="area">Площадь</label>
                            <input class="form-control" type="number" step="0.01" min="0" id="area" name="area" value="${parkEdit.area}" required>
                        </div>
                        <div class="mb-3 form-check">
                            <input class="form-check-input" type="checkbox" id="hasWater" name="hasWater" ${parkEdit.hasWater ? 'checked' : ''}>
                            <label class="form-check-label" for="hasWater">Есть водоем</label>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="buildDate">Дата постройки</label>
                            <input class="form-control" type="date" id="buildDate" name="buildDate" value="${parkEdit.buildDate}" required>
                        </div>
                        <button class="btn btn-primary" type="submit">Сохранить</button>
                        <a class="btn btn-secondary" href="<c:url value='/park' />">Отменить</a>
                    </form>
                </div>
            </section>
        </div>
    </main>
    <jsp:include page="/views/footer.jsp" />
</body>
</html>

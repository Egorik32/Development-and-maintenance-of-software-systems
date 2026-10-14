<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Парки</title>
    <link rel="stylesheet" href="<c:url value='/css/bootstrap.min.css' />">
    <link rel="stylesheet" href="<c:url value='/css/style.css' />">
    <script defer src="<c:url value='/js/bootstrap.min.js' />"></script>
</head>
<body>
    <jsp:include page="/views/header.jsp" />
    <main class="container-fluid">
        <h1 class="page-title">Парки</h1>
        <div class="row g-3">
            <section class="col-lg-8">
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
                                <th>Действия</th>
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
                                    <td class="table-actions">
                                        <a class="btn btn-sm btn-outline-primary" href="<c:url value='/editpark?id=${park.id}' />">Редактировать</a>
                                        <a class="btn btn-sm btn-outline-danger" onclick="return confirm('Удалить запись?')" href="<c:url value='/deletepark?id=${park.id}' />">Удалить</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>
            <section class="col-lg-4">
                <div>
                    <h2 class="h4">Новый парк</h2>
                    <form method="post" action="">
                        <div class="mb-3">
                            <label class="form-label" for="name">Название</label>
                            <input class="form-control" type="text" id="name" name="name" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="type">Тип</label>
                            <input class="form-control" type="text" id="type" name="type" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="area">Площадь</label>
                            <input class="form-control" type="number" step="0.01" min="0" id="area" name="area" required>
                        </div>
                        <div class="mb-3 form-check">
                            <input class="form-check-input" type="checkbox" id="hasWater" name="hasWater">
                            <label class="form-check-label" for="hasWater">Есть водоем</label>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="buildDate">Дата постройки</label>
                            <input class="form-control" type="date" id="buildDate" name="buildDate" required>
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

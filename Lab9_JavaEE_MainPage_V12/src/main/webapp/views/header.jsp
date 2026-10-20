<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand" href="<c:url value='/' />"><img src="<c:url value='/images/park.png' />" width="32" height="32" alt="Города и парки"> Города и парки</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarContent">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link" href="<c:url value='#' />">Города</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="<c:url value='#' />">Парки</a>
                </li>
            </ul>
        </div>
    </div>
</nav>

<%@ page language="java" contentType="text/html; charset=UTF-8"
		 pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">

	<!-- jQuery library -->
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
	<!-- Latest compiled JavaScript -->
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
	<meta charset="UTF-8">
	<title>Title</title>
</head>
<body>
<style>
	body {
		margin: 2rem;
	}
</style>
<h3>File Upload:</h3>
Upload a .csv file: <br />
<br />
<form method="POST" enctype="multipart/form-data" action="/load">
	<input type="file" multiple="multiple" name="files"/>
	<br/>
	<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
	<%-- Progress Section --%>
	<c:if test="${showProgress}">
		<p id="progress-text">Uploading and processing</p>
		<img id="progress-img" src="https://upload.wikimedia.org/wikipedia/commons/b/b1/Loading_icon.gif" width="200px" alt="Loading..."/>
		<script>
			function checkProgress() {
				fetch('/checkProgress')
						.then(response => response.json())
						.then(data => {
							if (!data.showProgress) {
								document.getElementById('progress-text').style.display = 'none';
								document.getElementById('progress-img').style.display = 'none';
							} else {
								setTimeout(checkProgress, 3000);
							}
						});
			}
			setTimeout(checkProgress, 1000);
		</script>
	</c:if>
	<br>
	<button type="submit" class="btn btn-primary">Load</button>
</form>
<br/>

<table class="table">
	<thead>
	<tr>
		<th scope="col">Filename</th>
		<th scope="col">Processing time</th>
		<th scope="col">Convert</th>
	</tr>
	</thead>
	<tbody>
	<form method="POST" enctype="multipart/form-data" action="/uploadFile">

	<c:forEach var="filename" items="${filenames}">
		<tr>
			<td>${filename.get("filename")}</td>
			<td>${filename.get("time")}</td>
			<td><button class="btn btn-primary" name="convertFile" type="submit" value='${filename.get("filename")}'>Convert</button></td>
		</tr>
	</c:forEach>
	</form>
	</tbody>
</table>

</body>
</html>

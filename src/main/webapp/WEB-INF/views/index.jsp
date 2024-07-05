<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>File Upload</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.13.0/css/all.min.css" rel="stylesheet">
    <link href="//cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css" rel="stylesheet" />
    <script src="//cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/js/toastr.min.js"></script>
    <style>
        body {
            margin: 2rem;
        }
        .container {
            margin-top: 2rem;
        }
        .table th, .table td {
            text-align: center;
            vertical-align: middle;
        }
        #progress-img {
            display: block;
            margin: 0 auto;
        }
    </style>
</head>
<body>
<div class="container">
    <h3>File Upload</h3>
    <p>Upload a .csv file:</p>
    <form method="POST" enctype="multipart/form-data" action="/load">
        <div class="form-group">
            <input type="file" multiple="multiple" name="files" class="form-control-file" accept=".csv"/>
        </div>
        <button type="submit" class="btn btn-primary"><i class="fa fa-download"></i> Load</button>
    </form>
    <br/>
    <form method="POST" action="/uploadAll">
        <button type="submit" class="btn btn-primary" style="float: right; margin-bottom: 2rem;"><i class="fa fa-upload"></i> Upload All</button>
    </form>
    <br/>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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

    <c:if test="${uploadSuccess}">
        <script>
            toastr.success('File(s) uploaded successfully!');
        </script>
    </c:if>

    <table class="table table-striped table-bordered">
        <thead>
        <tr>
            <th scope="col">Filename</th>
            <th scope="col">Processing time</th>
            <th scope="col">Upload</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="file" items="${filenames}">
            <tr>
                <td>${file.filename}</td>
                <td><c:if test="${file.uploadTime > 0}">${file.uploadTime} ms</c:if></td>
                <td>
                    <form method="POST" action="/uploadFile">
                        <c:choose>
                            <c:when test="${!file.isUploaded}">
                                <button class="btn btn-primary" name="convertFile" type="submit" value="${file.filename}"><i class="fa fa-upload"></i> Upload</button>
                            </c:when>
                            <c:otherwise>
                                Uploaded
                            </c:otherwise>
                        </c:choose>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>

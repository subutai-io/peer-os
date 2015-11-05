
<!DOCTYPE html>
<html>
<head>

  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Subutai | Login</title>
  <link href="resources/css/style.css" rel="stylesheet">

</head>
<body>
<div class="b-login-layout">
  <div class="b-login">
    <div class="b-login__title">
      <div class="b-login-title">
        <div class="b-login-title__welcom">
          Error
        </div>
        <div class="clear">
          <%
            //****************************************************
            if(request.getAttribute( "error" )!=null)
            {
              String error = (String)request.getAttribute( "error" );
              out.println( "<label class=\"error\">"+error+"</label>");
            }
            //****************************************************
          %>
        </div>
      </div>
    </div>
  </div>
</div>
</body>
</html>

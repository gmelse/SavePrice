<?php 

// This file contains the database access information. 

// This file also establishes a connection to MySQL 

// and selects the database.


// Set the database access information as constants:

DEFINE ('DB_USER', 'root');

DEFINE ('DB_PASSWORD', '123789mysql');

DEFINE ('DB_HOST', 'localhost');

DEFINE ('DB_NAME', 'Android_DB3');



// Make the connection:

$dbc = @mysqli_connect (DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);

//mysqli_query("SET NAMES 'greek'", $dbc);
//mysqli_query("SET CHARACTER SET 'greek'", $dbc);
//mysql_set_charset('utf8', $dbc);
//mysqli_query ("SET NAMES 'UTF8'", $dbc);
//mysqli_query ("SET CHARACTER SET 'UTF8'", $dbc);
//$mysqli->query("SET NAMES 'utf8'",$dbc);

mysqli_query($dbc, "SET NAMES 'utf8'");

if (!$dbc) {

	trigger_error ('Could not connect to MySQL: ' . mysqli_connect_error() );

}


?>

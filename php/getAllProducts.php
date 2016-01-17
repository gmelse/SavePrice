<?php
 
/*
 * Following code will list all the products
 */
 
// array for JSON response
$response = array();
 
// include db connect class
//require_once __DIR__ . '/mysqli_connect.php';
 
// connecting to db
//$db = new DB_CONNECT();

define ('MYSQL', 'mysqli_connect.php');
require_once (MYSQL);

//$searchTerm = base64_decode($_POST['searchTerm']);
 
// get all products from products table

//$q= "SELECT * FROM players WHERE firstname LIKE '%test%' OR lastname LIKE '%test%'";

  //$q = "SELECT * FROM product_names WHERE Product_Name LIKE '%.$searchTerm.%'";
  //$q = 'SELECT * FROM `product_names` WHERE `Product_Name` LIKE '%.$searchTerm.%'';
  $q ='SELECT * FROM `product_names` LIMIT 5';

$result = mysqli_query($dbc, $q) or trigger_error("Query: $q\n<br />MySQL Error: " . mysqli_error($dbc)); //ORDER BY Product_Name DESC LIMIT 0,5
 
// check for empty result
if (mysqli_num_rows($result) > 0) {
    // looping through all results
    // products node
    $response["products"] = array();
 
    while ($row = mysqli_fetch_array($result)) {
        // temp user array
        $product = array();
        //$product["pid"] = $row["pid"];
        $product["name"] = $row["Product_Name"];
        //$product["price"] = $row["price"];
        //$product["created_at"] = $row["created_at"];
        //$product["updated_at"] = $row["updated_at"];
 
        // push single product into final response array
        array_push($response["products"], $product);
    }
    // success
    $response["success"] = 1;
 
    // echoing JSON response
    echo json_encode($response);
} else {
    // no products found
    $response["success"] = 0;
    $response["message"] = "No products found";
 
    // echo no users JSON
    echo json_encode($response);
}
?>

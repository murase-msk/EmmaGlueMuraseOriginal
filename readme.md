# description


glueサーバを作るかも


* http://localhost:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticStroke
	- glueをstrokeにしてみた

* http://localhost:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticRoad
	- focus,glue,contexっぽい地図を作製した

* http://localhost:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawSimpleRoad
	- DBから道路データを取得し，描画後，画像データとしてクライアントに返す

* http://localhost:8080/EmmaGlueMuraseOriginal/MainServlet?type=Test
	- サーブレットで描画した図形を画像データとしてクライアントに返す


# バージョン履歴

## v2_0_2
細かい修正

## v2_0_0

* 緯度経度，投影法の変換をPostGISでなくjavaで処理することで高速化した(LngLatMercatorUtility).

DrawElasticRoad

* オリジナルのglueサーバと同じように道路変形をする

## v1_0_0

DrawElasticRoad

* contextからfocusにつながるようにスケールを多段にする(すべての道路)

DrawElasticStroke

* contextからfocusにつながるようにスケールを多段にする(ストロークを使う)

DrawSimpleRoad

* 単純な道路の描画

Test

* 描画のテスト

同心円，放射方向に対してスケール変化する


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

v2_0_0
DrawElasticRoad
元のglueサーバに近い道路変形をする(ベジェ曲線使ってないかも)

v1_0_0
DrawElasticStroke
DrawElasticRoad
DrawSimpleRoad
Test
同心円，放射方向に対してスケール変化する



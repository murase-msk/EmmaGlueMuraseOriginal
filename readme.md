# 概要


OSM版glueサーバ
詳細
<http://rain.elcom.nitech.ac.jp/tsg-wiki/index.php?GlueServerOsm>


# システム詳細
## 共通のリクエストパラメータ

|パラメータ|省略可能か|説明|具体値|
|:--|:--|:--|:--|
|centerLngLat|×|中心の緯度経度|137.088,35.172|
|focus_zoom_level|×|フォーカスのズームレベル|16|
|context_zoom_level|×|コンテキストのズームレベル|15|
|glue_inner_radius|×|glue内側の半径(px)|125|
|glue_outer_radius|×|glue外側の半径(px)|200|


## ConvertElasticPoints
緯度経度座標の任意の点や線などをglue上に描画できるように変換する

### リクエストパラメータ
|パラメータ|省略可能か|説明|具体値|
|:--|:--|:--|:--|
|type|×|glueの描画方法|ElasticPointsを指定|
|points|×|変換する点系列(緯度経度座標で指定し，x1,y1,x2,y2,...のように書く)|136.92,35.15,136.926,35.15|

### レスポンス
xy: glue画像の左上を(0,0)としたxyの座標

### サンプルリクエスト
<http://rain2.elcom.nitech.ac.jp:8080/EmmaGlueMuraseOriginal/MainServlet?type=ConvertElasticPoints&centerLngLat=136.9288336363183,35.158167325045824&points=136.92588320639172,35.15937778672364,136.9266127672479,35.15893921573326,136.9270526495242,35.15869361494546,136.92767492201676,35.15823749722958,136.92857614424028,35.15764103174276,136.92920914557283,35.15704456188256,136.92994943525997,35.15660597831222,136.93057170775256,35.15601827262135,136.93055025008164,35.156000729102885&focus_zoom_level=17&context_zoom_level=15&glue_inner_radius=200&glue_outer_radius=300>

## DrawGlue_v2
ストロークを使った新しいglue道路選択手法
村瀬の修論で記述予定

### リクエストパラメータ
|パラメータ|省略可能か|説明|具体値|
|:--|:--|:--|:--|
|type|×|glueの描画方法|DrawGlue_v2を指定|
|option|○|glueの描画方法|vectorを指定するとxml形式のベクターデータを返す|

### サンプルリクエスト
<http://rain2.elcom.nitech.ac.jp:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawGlue_v2&centerLngLat=136.9369411468506,35.15805494125627&focus_zoom_level=16&context_zoom_level=15&glue_inner_radius=125&glue_outer_radius=200&roadType=car>
<http://rain2.elcom.nitech.ac.jp:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawGlue_v2&centerLngLat=136.9369411468506,35.15805494125627&focus_zoom_level=16&context_zoom_level=15&glue_inner_radius=125&glue_outer_radius=200&roadType=car&option=vector>

### option=vectorを指定したときのレスポンス
|パラメータ|説明|
|:--|:--|
|selectedStrokeId|選択されたストロークのID&br;("osm_road_db"データベースの"stroke_v2.flatted_stroke_table"テーブルの"id")|
|selectedTransformedPoint| glue画像の左上を(0,0)としたxy座標を返す|
|selectedTransfromedLngLat|選択されたストロークの緯度経度|
|roadClazz|選択されたストロークの道路クラス(道路クラスについてはtk-teraの\tsg\資料\時空間rain2データベース覚書_OSM編.xlsxを参照)|


## DrawMitinariSenbetuAlgorithm
小関さんの道なり道路選別手法をOSMで実装

### リクエストパラメータ
|パラメータ|省略可能か|説明|具体値|
|:--|:--|:--|:--|
|type|×|glueの描画方法|DrawMitinariSenbetuAlgorithmを指定|
|option|○|glueの描画方法|vectorを指定するとxml形式のベクターデータを返す|

### サンプルリクエスト
<http://rain2.elcom.nitech.ac.jp:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawMitinariSenbetuAlgorithm&centerLngLat=136.9365119934082,35.158405798185974&focus_zoom_level=16&context_zoom_level=15&glue_inner_radius=125&glue_outer_radius=200&roadType=car>
<http://rain2.elcom.nitech.ac.jp:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawMitinariSenbetuAlgorithm&centerLngLat=136.9365119934082,35.158405798185974&focus_zoom_level=16&context_zoom_level=15&glue_inner_radius=125&glue_outer_radius=200&roadType=car&option=vector>
### option=vectorを指定したときのレスポンス
|パラメータ|説明|
|:--|:--|
|selectedLinkId|選択されたリンクのID&br;("osm_road_db"データベースの"public.osm_japan_car_2po_4pgr"テーブルの"id")|
|selectedTransformedPoint| glue画像の左上を(0,0)としたxy座標を返す|
|selectedTransformedLngLat|選択されたリンクの緯度経度|
|roadClazz|選択されたストロークの道路クラス&br;(道路クラスについてはtk-teraの\tsg\資料\時空間rain2データベース覚書_OSM編.xlsxを参照)|

## DrawElasticRoad
すべての道路をglueに描画

### リクエストパラメータ
|パラメータ|省略可能か|説明|具体値|
|:--|:--|:--|:--|
|type|×|glueの描画方法|DrawElasticRoadを指定|
|roadType|○|表示する道路の種類|allかcar(デフォルト値car)|
|isDrawPolygon|○|ポリゴンの描画をするか|trueかfalse(デフォルト値false)|

### サンプルリクエスト
[すべての道路を描画](http://rain2.elcom.nitech.ac.jp:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticRoad&centerLngLat=137.08877563476562,35.17229866784575&focus_zoom_level=16&context_zoom_level=15&glue_inner_radius=125&glue_outer_radius=200&roadType=all)
[ポリゴンも描画](http://rain2.elcom.nitech.ac.jp:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticRoad&centerLngLat=137.08877563476562,35.17229866784575&focus_zoom_level=16&context_zoom_level=15&glue_inner_radius=125&glue_outer_radius=200&roadType=all&isDrawPolygon=true)
[道路クラスが一定以上の道路のみを描画](http://rain2.elcom.nitech.ac.jp:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticRoad&centerLngLat=136.93586826324463,35.15847596957192&focus_zoom_level=16&context_zoom_level=15&glue_inner_radius=125&glue_outer_radius=200&roadType=car)
[ベクター形式で返す](http://rain2.elcom.nitech.ac.jp:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticRoad&centerLngLat=137.08877563476562,35.17229866784575&focus_zoom_level=16&context_zoom_level=15&glue_inner_radius=125&glue_outer_radius=200&option=vector)

## DrawElasticStroke_v2
上位5割のストロークを選択し表示する

### リクエストパラメータ
|パラメータ|省略可能か|説明|具体値|
|:--|:--|:--|:--|
|type|×|glueの描画方法|DrawElasticStroke_v2を指定|

### サンプルリクエスト
<http://rain2.elcom.nitech.ac.jp:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticStroke_v2&centerLngLat=136.9309671669116,35.15478942665804&focus_zoom_level=17&context_zoom_level=15&glue_inner_radius=200&glue_outer_radius=300>








# ソースコードについて
詳しくはdoc/index.htmlを見てください

# バージョン履歴
書くのめんどくさくなったのでコミットメッセージ見てください

## v2_1_3
DrawSimplifiedStroke：line simplification機能の追加

## v2_1_2
DrawElasticRoad：道路に色を付けた

## v2_1_1
ストロークに対応


## v2_1_0
URLのパラメータの設定可能

<http://rain2.elcom.nitech.ac.jp:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticRoad&centerLngLat=136.9309,35.1547&focus_zoom_level=17&context_zoom_level=15&glue_inner_radius=200&glue_outer_radius=300&roadType=car>


## v2_0_3
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


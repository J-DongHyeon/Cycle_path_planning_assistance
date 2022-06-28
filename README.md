<h1 align="center">  Cycle Path Planning Assistance </h1>

<h4 align="center"> 사이클 경로 탐색에 도움을 주는 IoT 어플리케이션 </h4>


<p align= "center">
<img src=/docs/IMAGE/cycle_image.PNG width=600 height=400></p> 

------------------------------------------

# 1. 프로젝트의 필요성
-- 사이클 사진
> &nbsp;사이클을 타는 것이 취미인 사람으로써, 사이클을 탈 때 날씨의 영향을 굉장히 많이 받는다고 생각한다. 풍향, 풍속, 강수 유무 등 어떠한 날씨 환경에서 사이클을 타느냐에 따라 그에 대한 만족도는 달라진다. 학계에 따르면, 1대의 자전거 뒤에서 공기저항을 줄이면서 주행할 경우 앞의 사람보다 26%가량의 힘을 아끼며 달릴 수 있다고 한다. 앞쪽과 좌우로 다른 자전거에 둘러싸였을 때는 최대 45%의 에너지 절감을 하며 주행할 수 있다고 한다. 이렇듯이 조금 더 만족스런 사이클 주행에 있어서 풍향, 풍속의 영향은 배제할 수 없는 요소이다. 또한, 강수 유무 역시 배제할 수 없는 요소이다. 우천 시 사이클 주행을 할 경우 사고 확률이 더욱 증가할 것이고, 사이클 본체가 녹스는 불상사가 생길 수 있다.
> 
> &nbsp;물론, 일기 예보를 미리 확인함으로써 위와 같은 불상사에 대해 예방할 수 있다. 하지만, 사이클 경로에 속하는 모든 지역의 일기 예보를 일일히 찾아보는 것은 굉장히 귀찮은 일이다. 경로가 길면 길수록 일기 예보를 찾아봐야 하는 지역은 더욱 많아질 것이고, 적합한 사이클 경로를 찾는 것이 생각보다 많은 노력과 시간이 든다. 이러한 문제를 해결하기 위해 사이클 경로 상의 여러 날씨 정보를 한눈에 볼 수 있는 어플리케이션을 제작하였다. 사용자는 이 어플리케이션을 통해 원하는 사이클 경로 상의 기온, 풍향, 풍속, 강수 확률 등의 데이터를 한눈에 확인할 수 있고, 더욱 간편하고 빠르게 사이클 경로 계획을 세울 수 있다. 또한, 최대 54시간 이후 까지의 날씨 데이터를 볼 수 있으므로 미래의 사이클 경로 계획을 세우는 데에도 도움이 될 수 있다.

------------------------------------------

# 2. 개발환경 & 사용한 API
-- node.js, mosquitto, mongodb 사진
* Web Server - IoT 네트워크에서 주로 사용되는 Node.js를 기반으로, express 웹 프레임워크를 이용하여 개발하였다. 
* MQTT - IoT 네트워크 상에서 날씨 데이터를 MQTT 프로토콜로 송수신 하였다. MQTT 오픈소스로 주로 사용되는 Mosquitto 를 설치하여 사용하였다.
* DB - 사용자가 검색한 출발지, 목적지 데이터를 저장하기 위해 MongoDB를 사용하였다.
* 사용한 API - 카카오 지도 API, 공공 데이터 포털의 단기예보 API

------------------------------------------

# 3. 프로젝트의 목적
> &nbsp;본 프로젝트의 최종적인 목표는 사용자가 원하는 시간대의 사이클 경로상 날씨 데이터 (기온, 풍향, 풍속, 강수 확률) 를 한눈에 보기 쉽게 제공하는 Web UI를 제작하는 것이다. Web UI에는 지도를 표현함으로써 사용자가 사이클 경로와 그 경로상의 날씨 데이터를 한눈에 볼 수 있도록 하였다. 지도를 표현하기 위하여 카카오 지도 API를 이용하였다. 그리고 각 지역의 날씨 데이터를 얻어오기 위해서는 공공 데이터 포털의 기상청 단기예보 API를 활용하였다. 단기 예보 API가 날씨 데이터를 수집하는 가상 센서 역할을 하는 것이다. Java로 구현한 MQTT client가 단기예보 API를 Web crawling 하여 받아오고 이를 MQTT 프로토콜로 Web Server의 MQTT client에 전송하는 구조이다.
> > ### Web crawling을 node.js에서 안하고 굳이 Java로 구현한 MQTT client에서 하는 이유
> > 사실 Node.js에서 단기예보 API를 Web crawling 하여 사용해도 문제될 것이 없다. 오히려 구조가 더욱 단순해 질 것이다. 하지만 Java로 구현한 MQTT client가 센서 역할을 하고 여기서 발행되는 데이터가 IoT 네트워크 상에서 송수신 되는 구성을 맞추기 위하여 Java MQTT client로 Web crawling하여 Web Server에 송신하는 구조로 만들었다. 만약 실제 센서를 이용했다고 하면 Node.js에서 이 센서 데이터를 직접 받아와 사용하는 것은 불가능하다. 센서를 관리하는 다른 MQTT client가 센서 데이터를 추출하여 IoT 네트워크 상에서 Publish 하고 Web Server가 이를 Subscribe 하는 구조가 되어야 한다. 즉, 이 프로젝트에서는 Java로 구현한 MQTT client는 가상 센서 역할을 하며 그 센서 데이터는 단기예보 API로 받아오는 것이다. 그리고 이 데이터를 IoT 네트워크 상에서 Publish 하고 Web Server가 이를 Subscribe 하는 구조이다. 이러한 IoT 네트워크 구조를 이해하고 연습하기 위해서 Node.js가 아닌 다른 MQTT client에서 Web crawling 하는 구조로 프로젝트를 구현하였다.

------------------------------------------

# 4. 시스템 구조

<p align= "center">
<img src=/docs/IMAGE/system_structure.PNG width=600 height=300></p>

> &nbsp;본 시스템은 가상 센서, MQTT client, MQTT Broker, Node.js, MongoDB, Web UI 로 구성된다. 사용자는 HTTP Server가 제공하는 Web UI에 원하는 자전거 경로와 시간대 정보를 입력한다. HTTP Server는 해당 경로 정보를 MongoDB 에 저장하고 Node.js의 MQTT client는 MQTT Broker 서버에 출발지, 목적지 정보와 시간대 정보를 Publish 한다. MQTT Broker 서버는 해당 Topic을 Subscribe 하는 다른 MQTT client 에게 전달한다. 해당 MQTT client는 가상 센서 (단기 예보 API) 로부터 기온, 풍향, 풍속, 강수 확률 데이터를 수집하고 MQTT Broker 서버에 Publish 한다. Node.js는 이 가상 센서 데이터를 Subscribe 하며, Web UI 지도에 사이클 경로를 표시하고 경로 상의 기온, 풍향, 풍속, 강수 확률 데이터를 나타낸다.

------------------------------------------

# 5. 동작 과정
-- sub 사진
> &nbsp;시스템의 전반적인 동작 과정은 다음과 같다. 먼저, Java로 구현한 MQTT client는 MQTT Broker 서버에게 출발지, 목적지 정보와 시간대 정보 Topic을 구독 요청하고, 구독 요청을 받은 MQTT Broker 서버는 이 MQTT client를 해당 Topic의 구독자 리스트에 추가한다. Topic 명은 'path' 이다. Node.js의 MQTT client는 MQTT Broker 서버에게 사이클 경로 상의 기온, 풍향, 풍속, 강수 확률 Topic의 구독을 요청하고, 구독 요청을 받은 MQTT Broker 서버는 이 MQTT client를 해당 Topic의 구독자 리스트에 추가한다. Topic명은 'result' 이다.

-- 지도 사진
> &nbsp;Web UI에는 출발지, 목적지를 입력하는 텍스트 박스와 지도가 나타나 있다. 사용자가 원하는 출발지, 목적지와 시간대를 입력하면 지도에 사이클 경로가 표시된다.

-- Mongodb 사진
> &nbsp;사용자가 원하는 사이클 경로를 입력하면 Node.js의 HTTP Server는 HTML 페이지로부터 출발지와 목적지, 시간대 정보를 socket으로 전송받는다. 그리고 출발지, 목적지 검색 기록을 MongoDB에 저장한다. 

-- pub 사진, 지도에 마커 있는 사진
> &nbsp;Node.js의 MQTT client는 출발지, 목적지 정보와 시간대 정보를 담은 Topic 'path' 를 Publish 한다. MQTT Broker는 해당 Topic을 구독하는 MQTT client에게 전송한다. 'path' Topic을 받은 MQTT client는 출발지와 목적지 간의 경로를 일정한 간격으로 나누고, 사용자가 요청한 시간대의 각 위치에서의 기온, 풍향, 풍속, 강수 확률 데이터를 가상 센서 (단기 예보 API) 로부터 수집한다. 그리고 이 데이터를 담은 Topic 'result' 를 Publish 한다. MQTT Broker는 해당 Topic을 구독하는 Node.js의 MQTT client에게 전송한다. Node.js는 사이클 경로 상의 각 위치에서의 날씨 데이터를 받으면 이를 한눈에 보기 쉽게 HTML 페이지의 지도에 마커로 표시한다.
------------------------------------------

# 6. 실행 결과

-- 실행 동영상

------------------------------------------

# 7. 한계점
> &nbsp;공공 데이터 포털에서 단기 예보 API를 가져올 때 XML 데이터가 굉장히 큰 편이다. 이 XML 데이터가 큰 이유 탓인지, 아니면 공공 데이터 포털의 응답 속도가 느린 탓인지는 정확히 모르겠으나 Web crawling 하는데에 시간이 많이 걸리는 편이다. 따라서 날씨 데이터를 지도에 표시하기까지 시간이 오래 걸리는 편이다.
> > (Jsoup의 Time out이 기본적으로 15s로 셋팅되어 있는데, 상황에 따라서 Web crawling 하는 시간이 굉장히 느린 경우 15s를 넘겨서 SocketTimeoutException 예외가 발생하는 경우도 가끔씩 다. 이 경우, connect() 함수 호출 시 timeout 시간을 넉넉하게 설정하면 해결된다.)
------------------------------------------

# 8. 참고 사이트 & 오픈소스
#### 📙 https://www.chosun.com/site/data/html_dir/2009/06/04/2009060400044.html </br>
#### 📙 https://www.data.go.kr/ </br>
#### 📙 http://www.tcpschool.com/html-input-types/intro </br>
#### 📙 https://nearhomedeveloper.tistory.com/entry/javajsoup-jsoup-Connect-time-out-error-%ED%95%B4%EA%B2%B0 </br>
#### 1️⃣ https://apis.map.kakao.com/web/sample </br>
#### 2️⃣ https://gist.github.com/fronteer-kr/14d7f779d52a21ac2f16 </br>
------------------------------------------

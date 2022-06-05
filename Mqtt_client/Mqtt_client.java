package MiniProject;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Mqtt_client implements MqttCallback{ // implement callback 추가 & 필요한 메소드 정의
	static MqttClient sampleClient;// Mqtt Client 객체 선언
	
    public static void main(String[] args) {
    	Mqtt_client obj = new Mqtt_client();
    	obj.run();
    }
    public void run() {    	
    	connectBroker(); // 브로커 서버에 접속
    	try { // 여기 추가
    		sampleClient.subscribe("led"); // LED 리소스 구독
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	while(true) {
    		try {
    			String[] weather_data  = get_weather_data(); // 공공 API
    	       	String pm_data = get_pm_data(); // 공공 API
    	       	publish_data("tmp", "{\"tmp\": "+weather_data[0]+"}"); // 온도 데이터 발행
    	       	publish_data("humi", "{\"humi\": "+weather_data[1]+"}"); // 습도 데이터 발행
    	       	publish_data("pm", "{\"pm\": "+pm_data+"}"); // 미세먼지 데이터 발행
    	       	Thread.sleep(5000); // @@@@@@
    		}catch (Exception e) {
				// TODO: handle exception
    			try {
    				sampleClient.disconnect();
				} catch (MqttException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    			e.printStackTrace();
    	        System.out.println("Disconnected");
    	        System.exit(0);
			}
    	}
    }
    
    public void connectBroker() {
        String broker = "tcp://127.0.0.1:1883"; // 브로커 서버의 주소 
        String clientId = "practice"; // 클라이언트의 ID
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            sampleClient = new MqttClient(broker, clientId, persistence);// Mqtt Client 객체 초기화
            MqttConnectOptions connOpts = new MqttConnectOptions(); // 접속시 접속의 옵션을 정의하는 객체 생성
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts); // 브로커서버에 접속
            sampleClient.setCallback(this);// Call back option 추가
            System.out.println("Connected");
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
    
    public void publish_data(String topic_input, String data) { // @@@@@ 스태틱 제거
        String topic = topic_input; // 토픽
        int qos = 0; // QoS level
        try {
            System.out.println("Publishing message: "+data);
            sampleClient.publish(topic, data.getBytes(), qos, false); // topic, 데이터를 byte로 변환하여 전송, qos level, retain bit
            System.out.println("Message published");
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
    
    public String[] get_weather_data() { // @@@@@ 스태틱 제거
    	// 현재 시간 확인해서 날짜, 시간 저장
    	Date current = new Date(System.currentTimeMillis());
    	SimpleDateFormat d_format = new SimpleDateFormat("yyyyMMddHHmmss"); 
    	//System.out.println(d_format.format(current));
    	String date = d_format.format(current).substring(0,8); // 날짜
    	String time = d_format.format(current).substring(8,10); // 시간
       	
    	String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst" // https가 아닌 http 프로토콜을 통해 접근해야 함.
    			+ "?serviceKey=X7VbxDZo%2F8scobmS5QUIF2h6s%2F2FVu4HbJ%2BSa2x31kXEuRx8j48OX79kZ4kGJ9F6jl7ef6Haq4SD2sK8t3Entw%3D%3D"
    			+ "&pageNo=1&numOfRows=1000"
    			+ "&dataType=XML"
    			+ "&base_date="+date
    			+ "&base_time="+time+"00"
    			+ "&base_time="+time+"00"
    			+ "&nx=73"
    			+ "&ny=134";
    	
    	//데이터를 저장할 변수 초기화
		String temp = "-99"; //데이터를 잘 못가져오는 경우 (ex 정각에 데이터를 가져오는 경우) 이 디폴트 값을 사용
		String humi = "-99";
				
    	Document doc = null;
		
		// Jsoup으로 API 데이터 가져오기
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(doc);
		
		Elements elements = doc.select("item");
		for (Element e : elements) {
			if (e.select("category").text().equals("T1H")) { // 기온데이터
				temp = e.select("obsrValue").text();
			}
			if (e.select("category").text().equals("REH")) { // 습도
				humi = e.select("obsrValue").text();
			}
		}
		String[] out = {temp, humi};
    	return out;
    }
    
    
    public String get_pm_data() { // @@@@@ 스태틱 제거
    	String url = "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/"
    			+ "getCtprvnRltmMesureDnsty"
    			+ "?serviceKey=X7VbxDZo%2F8scobmS5QUIF2h6s%2F2FVu4HbJ%2BSa2x31kXEuRx8j48OX79kZ4kGJ9F6jl7ef6Haq4SD2sK8t3Entw%3D%3D"
    			+ "&returnType=xml"
    			+ "&numOfRows=100"
    			+ "&pageNo=1"
    			+ "&sidoName=%EA%B0%95%EC%9B%90" // 강원
    			+ "&ver=1.0"; 
		String value = "";
    	Document doc = null;
		
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(doc);
		Elements elements = doc.select("item");
		for (Element e : elements) {
			if (e.select("stationName").text().equals("옥천동")) {
				value = e.select("pm10Value").text();
			}
		}
    	return value;
    }
    ///@@@@@@@@@@@@@@@@@
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		System.out.println("Connection lost");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		// TODO Auto-generated method stub
		if (topic.equals("led")){
			System.out.println("--------------------Actuator Function--------------------");
			System.out.println("LED Display changed");
			System.out.println("LED: " + msg.toString());
			System.out.println("---------------------------------------------------------");
		}		
	}
}
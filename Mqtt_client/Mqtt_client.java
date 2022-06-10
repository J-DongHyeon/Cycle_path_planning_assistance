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

import java.util.Arrays;

public class Mqtt_client implements MqttCallback{ // implement callback 추가 & 필요한 메소드 정의
	static MqttClient mqtt_client;// Mqtt Client 객체 선언
	
    public static void main(String[] args) {
    	Mqtt_client obj = new Mqtt_client();
    	obj.run();
    }
    public void run() {    	
    	connectBroker(); // 브로커 서버에 접속
    	try { 
    		mqtt_client.subscribe("path"); // path 리소스 구독
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    public void connectBroker() {
        String broker = "tcp://127.0.0.1:1883"; // 브로커 서버의 주소 
        String clientId = "weather publisher"; // 클라이언트의 ID
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mqtt_client = new MqttClient(broker, clientId, persistence);// Mqtt Client 객체 초기화
            MqttConnectOptions connOpts = new MqttConnectOptions(); // 접속시 접속의 옵션을 정의하는 객체 생성
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            mqtt_client.connect(connOpts); // 브로커서버에 접속
            mqtt_client.setCallback(this);// Call back option 추가
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
    
    public void publish_data(String topic_input, String data) { 
        String topic = topic_input; // 토픽
        int qos = 1; // QoS level
        try {
        	//String sub_data = "{\"test\":10}";
            System.out.println("Publishing message: "+ data);
            
            mqtt_client.publish(topic, data.getBytes(), qos, false); // topic, 데이터를 byte로 변환하여 전송, qos level, retain bit
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
    
    public double[][] get_weather_data(String[] time_lat_lng) {
    	
    	String plus_time = time_lat_lng[0];
    	double [] start_lat_lng = {Double.parseDouble(time_lat_lng[1]), Double.parseDouble(time_lat_lng[2])};
    	double [] dest_lat_lng = {Double.parseDouble(time_lat_lng[3]), Double.parseDouble(time_lat_lng[4])};
    	int [] start_xy = get_xy(start_lat_lng);
    	int [] dest_xy = get_xy(dest_lat_lng);

    	
    	int x_interval = Math.abs(start_xy[0] - dest_xy[0]);
    	int y_interval = Math.abs(start_xy[1] - dest_xy[1]);
    	
    	//출발지와 목적지 사이의 일정한 간격 좌표들을 구할 것이다. 거리가 너무 멀면 9개 좌표로 제한한다.
    	int [][] nx_ny = new int[9][2];
    	nx_ny[0][0] = start_xy[0]; nx_ny[0][1] = start_xy[1];
    	
    	//출발지 좌표가 목적지 좌표보다 더 클수도 있고 작을수도 있다.
    	//즉, 중간 좌표들은 출발지 좌표로부터 점점 증가할 수도 있고 감소할 수도 있다. 
    	int x_flag, y_flag;
    	if (start_xy[0] < dest_xy[0]) x_flag = 1;
    	else x_flag = -1;
    	
    	if (start_xy[1] < dest_xy[1]) y_flag = 1;
    	else y_flag = -1;
    	
    	//출발지와 목적지 간의 x좌표 간격이 y좌표 간격보다 크면 x좌표 간격을 기준으로 반복문을 돌려 중간 좌표들을 구한다.
    	//y좌표는 x좌표 증가량에 비례하여 증가한다.
    	//반대의 경우는 y좌표를 기준으로 중간 좌표들을 구한다.
    	if ((x_interval < 8) && (y_interval < 8)) {
    		if (x_interval > y_interval) {
    			for(int i=1, j=0; i<=x_interval; i++) {
    				nx_ny[i][0] = start_xy[0] + (i*x_flag);
    				if (((double)i / x_interval) > ((double)j / y_interval)) {
    					nx_ny[i][1] = start_xy[1] + ((++j)*y_flag);
    				} else {
    					nx_ny[i][1] = start_xy[1] + (j*y_flag);
    				}	
    			}
    		} else {
    			for(int i=1, j=0; i<=y_interval; i++) {
    				nx_ny[i][1] = start_xy[1] + (i*y_flag);
    				if (((double)i / y_interval) > ((double)j / x_interval)) {
    					nx_ny[i][0] = start_xy[0] + ((++j)*x_flag);
    				} else {
    					nx_ny[i][0] = start_xy[0] + (j*x_flag);
    				}	
    			}
    		}
    	} else if ((x_interval >= 8) && (y_interval < 8)) {
    		double sub_interval = (double)x_interval / 8;
    		double base_nx;
    		for(int i=1, j=0; i<=8; i++) {
				base_nx = start_xy[0] + ((i*sub_interval)*x_flag);
				nx_ny[i][0] = (int) base_nx;
				if (((double)i / 8) > ((double)j / y_interval)) {
					nx_ny[i][1] = start_xy[1] + ((++j)*y_flag);
				} else {
					nx_ny[i][1] = start_xy[1] + (j*y_flag);
				}	
			}
    	} else if ((x_interval < 8) && (y_interval >= 8)) {
    		double sub_interval = (double)y_interval / 8;
    		double base_ny;
    		for(int i=1, j=0; i<=8; i++) {
				base_ny = start_xy[1] + ((i*sub_interval)*y_flag);
				nx_ny[i][1] = (int) base_ny;
				if (((double)i / 8) > ((double)j / x_interval)) {
					nx_ny[i][0] = start_xy[0] + ((++j)*x_flag);
				} else {
					nx_ny[i][0] = start_xy[0] + (j*x_flag);
				}	
			}
    	} else {
    		double sub_x_interval = (double)x_interval / 8;
    		double sub_y_interval = (double)y_interval / 8;
    		double base_nx;
    		double base_ny;
    		for(int i=1; i<=8; i++) {
				base_nx = start_xy[0] + ((i*sub_x_interval)*x_flag);
				base_ny = start_xy[1] + ((i*sub_y_interval)*y_flag);
				nx_ny[i][0] = (int) base_nx;
				nx_ny[i][1] = (int) base_ny;		
			}
    	}
    	
    	// 현재 시간 확인해서 날짜, 시간 저장
    	Date current = new Date(System.currentTimeMillis());
    	SimpleDateFormat d_format = new SimpleDateFormat("yyyyMMddHHmmss"); 
    	int inow_date = Integer.parseInt(d_format.format(current).substring(0,8)); //현재 날짜
    	int ibase_date = inow_date;
    	String now_time = d_format.format(current).substring(8,12); //현재 시간
    	String base_time = now_time;
    	
    	// 공공 api에 데이터를 요청할 때는 02시, 05시, 08시, 11시, 14시, 17시, 20시, 23시를 기준으로 요청해야 한다.
    	// 요청한 시간을 기준으로 최대 70시간 이후까지의 단기예보 정보를 얻을 수 있다.
    	// 현재 시간과 가장 가까운 요청 시간을 구한다. (base_time)
    	if ((base_time.compareTo("0215") >= 0) && (base_time.compareTo("0515") < 0)) base_time = "0200";
    	else if ((base_time.compareTo("0515") >= 0) && (base_time.compareTo("0815") < 0)) base_time = "0500";
    	else if ((base_time.compareTo("0815") >= 0) && (base_time.compareTo("1115") < 0)) base_time = "0800";
    	else if ((base_time.compareTo("1115") >= 0) && (base_time.compareTo("1415") < 0)) base_time = "1100";
    	else if ((base_time.compareTo("1415") >= 0) && (base_time.compareTo("1715") < 0)) base_time = "1400";
    	else if ((base_time.compareTo("1715") >= 0) && (base_time.compareTo("2015") < 0)) base_time = "1700";
    	else if ((base_time.compareTo("2015") >= 0) && (base_time.compareTo("2315") < 0)) base_time = "2000";
    	else if ((base_time.compareTo("2315") >= 0) && (base_time.compareTo("2400") < 0)) base_time = "2300";
    	else if ((base_time.compareTo("0000") >= 0) && (base_time.compareTo("0215") < 0)) {
    		base_time = "2300"; ibase_date--;
    	}
    	
    	String base_date = String.valueOf(ibase_date);
    	
    	//요청 받을 공공 api 데이터 중에서 추출하고자 하는 데이터의 날짜, 시간을 구한다. (fcst_date, fcst_time)
    	String fcst_date;
    	String fcst_time;
    	if (plus_time.equals("now") || plus_time.equals("none")) {
    		if (Integer.parseInt(base_time.substring(0, 2)) == Integer.parseInt(now_time.substring(0, 2))) {
        		if (now_time.substring(0, 2) == "23") {
        			fcst_time = "0000";
        			fcst_date = String.valueOf(inow_date + 1);
        		}
        		else {
        			fcst_time = (Integer.parseInt(now_time.substring(0, 2)) + 1) + "00";
        			if (fcst_time.length() == 3) fcst_time = "0" + fcst_time;
        			fcst_date = String.valueOf(inow_date);

        		}
        	} else {
        		fcst_time = now_time.substring(0, 2) + "00";
        		fcst_date = String.valueOf(inow_date);
        	}
    	} else {
    		int iplus_time = 0;
    		
    		if (plus_time.length() == 4) {
        		iplus_time = plus_time.charAt(0) - '0';
        	} else if (plus_time.length() == 5) {
        		iplus_time = Integer.parseInt(plus_time.substring(0, 2));
        	}
    		
    		int sum = Integer.parseInt(now_time.substring(0, 2)) + iplus_time;
    		
    		if (sum / 24 > 0) fcst_date = String.valueOf(inow_date + (sum / 24));
    		else fcst_date = String.valueOf(inow_date);
    		
    		fcst_time = String.valueOf(sum % 24) + "00";
    		if (fcst_time.length() == 3) fcst_time = "0" + fcst_time;
    	}
    
    
    	//추출한 데이터를 담을 배열이다. (x좌표, y좌표, 1시간 기온, 풍향, 풍속, 강수 확률
    	double[][] result = new double [9][6]; 
    	
    	System.out.println("now_date : " + String.valueOf(inow_date) + ", now_time : " + now_time);
		System.out.println("base_date : " + base_date + ", base_time : " + base_time);
		System.out.println("fcst_date : " + fcst_date + ", fcst_time : " + fcst_time);
    	
		
		
		
    	for (int i=0; i<nx_ny.length; i++) {
    		if (nx_ny[i][0] == 0) break;
    		
    		result[i][0] = nx_ny[i][0];
    		result[i][1] = nx_ny[i][1];
    		
    		String nx = String.valueOf(nx_ny[i][0]);
    		String ny = String.valueOf(nx_ny[i][1]);
        	String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst" // https가 아닌 http 프로토콜을 통해 접근해야 함.
        			+ "?serviceKey=Lcm5d0LdcOf0ikmOlbmHQkrgM%2Fe%2Bl8laBOhOhXB4n9q8cOvOFyhnFvwKclSTvc%2BK3rll9BgV0dfGF9mdgnJGQA%3D%3D"
        			+ "&pageNo=1&numOfRows=1000"
        			+ "&dataType=XML"
        			//+ "&base_date=20220609"
        			//+ "&base_time=2300"
        			+ "&base_date="+base_date
        			+ "&base_time="+base_time
        			+ "&nx=" + nx
        			+ "&ny=" + ny;
    				
        	Document doc = null;
        
    		
    		// Jsoup으로 API 데이터 가져오기
    		try {
    			doc = Jsoup.connect(url).get();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		
	
    		//받아온 xml API 중 원하는 날짜 시간의 tmp, vec, wsd, pop 데이터 추출
    		Elements elements = doc.select("item");
    		for (Element e : elements) {
    			if (e.select("fcstDate").text().equals(fcst_date) && e.select("fcstTime").text().equals(fcst_time)) {
    				if (e.select("category").text().equals("TMP")) {
    					result[i][2] = Double.parseDouble(e.select("fcstValue").text());
    					System.out.print("nx : " + result[i][0] + ", ny : " + result[i][1]);
    					System.out.print(", TMP : " + result[i][2]);
    				}
    				else if (e.select("category").text().equals("VEC")) {
    					result[i][3] = Double.parseDouble(e.select("fcstValue").text());
    					System.out.print(", VEC : " + result[i][3]);
    				}
    				else if (e.select("category").text().equals("WSD")) {
    					result[i][4] = Double.parseDouble(e.select("fcstValue").text());
    					System.out.print(", WSD : " + result[i][4]);
    				}
    				else if (e.select("category").text().equals("POP")) {
    					result[i][5] = Double.parseDouble(e.select("fcstValue").text());
    					System.out.println(", POP : " + result[i][5]);
    				}
    			}
    		}
    	}
    	
    	System.out.println("Web Crawling Ok");
    	
    	return result;
    }
    
    //위도, 경도 정보를 받아 격자 x, y 좌표로 변환 (오픈소스 이용)
    //공공 api를 요청할 때 격자 x, y 좌표로 요쳥해야 한다. 
    public int[] get_xy(double[] lat_lng) {
    	double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기준점 Y좌표(GRID)
        
        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;
        int rs[] = {0, 0};
       
        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        
       
        double ra = Math.tan(Math.PI * 0.25 + (lat_lng[0]) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lat_lng[1] * DEGRAD - olon;
        
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;
        
        rs[0] = (int) (Math.floor(ra * Math.sin(theta) + XO + 0.5));
        rs[1] = (int) (Math.floor(ro - ra * Math.cos(theta) + YO + 0.5));
        
        return rs;
    }
    
    //x, y 좌표 정보를 받아 위도, 경도 정보로 변환 (오픈소스 이용)
    //web server의 Mqtt client에 publish 할 때는 위도, 경도 정보로 publish 할 것이다. 
    public double[] get_lat_lng(double[] xy) {
    	
    	double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기준점 Y좌표(GRID)
        
        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;
        double rs[] = {0, 0};
       
        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        
        double xn = xy[0] - XO;
        double yn = ro - xy[1] + YO;
        double ra = Math.sqrt(xn * xn + yn * yn);
        if (sn < 0.0) ra = -ra;
        double alat = Math.pow((re * sf / ra), (1.0 / sn));
        alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

        double theta = 0.0;
        if (Math.abs(xn) <= 0.0) {
            theta = 0.0;
        }
        else {
            if (Math.abs(yn) <= 0.0) {
                theta = Math.PI * 0.5;
                if (xn < 0.0) theta = -theta;
            }
            else theta = Math.atan2(xn, yn);
        }
        double alon = theta / sn + olon;
        rs[0] = alat * RADDEG;
        rs[1] = alon * RADDEG;
        
        return rs;
    }
    
    
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
		if (topic.equals("path")){
			System.out.println("--------------------Received Message---------------------");
			System.out.println("Path : " + msg.toString());
			System.out.println("---------------------------------------------------------");
			String[] time_lat_lng = msg.toString().split(" ");
			
			
			double[][] result = get_weather_data(time_lat_lng);
			
			// 최종 publish할 문자열
			String pub_rst = "";
			
			// 각 위치의 격자 x, y 좌표 정보를 위도, 경도 정보로 변환 (카카오 지도에 표시할 때는 위도, 경도 정보가 필요)
			for (int i=0; i<result.length; i++) {
				if (result[i][0] == 0) break;
				double [] sub_xy = {result[i][0], result[i][1]}; 
				double [] sub_lat_lng = get_lat_lng(sub_xy);
				result[i][0] = sub_lat_lng[0];
				result[i][1] = sub_lat_lng[1];
				
				pub_rst += result[i][0] + " " + result[i][1] + " " + result[i][2] + " " + result[i][3]
						 	+ " " + result[i][4] + " " + result[i][5] + "&";
			}
			
			pub_rst = pub_rst.substring(0, pub_rst.length() - 1);		

			
			// web server의 Mqtt client 로 '위도, 경도, 온도, 풍향, 풍속, 강수 확률' 데이터 publish
			try {
    			publish_data("result", pub_rst);
    		}catch (Exception e) {
				// TODO: handle exception
    			try {
    				mqtt_client.disconnect();
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
}
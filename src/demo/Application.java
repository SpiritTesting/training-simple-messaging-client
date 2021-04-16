package demo;

public class Application {

	public static void main(String[] args) throws Exception {
		MessageClient client = new MessageClient();
		System.out.println(client.readAllUsers());
		//System.out.println(client.registerUser("Gundel Gaukeley", "meinPasswort"));
		//System.out.println(client.readAllUsers());
		System.out.println(client.login(6,  "meinPasswort"));
		System.out.println(client.readUserDetails(6));
		
		System.out.println(client.readAllMessages());
		
//		while (true) {
//			int size = client.readAllMessages().size();
//			System.out.println(size + " messages in total");
//			Thread.sleep(2000);
//		}
	}

}

package project02;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ReservedBookControl {
	class A{
		public final ClientInterface c;
		public final Optional<Long> initTime;
		public A(ClientInterface c, Optional initTime) {
			super();
			this.c = c;
			this.initTime = initTime;
		}
	}
	// 				bookId, who     
	private final Map<Long, A> booksReserved;
	private final Map<ClientInterface, Long> clientDeception;
	
	public ReservedBookControl(){
		this.booksReserved   = new HashMap<Long, A>();
		this.clientDeception = new HashMap<ClientInterface, Long>(); 
	}
	
	private boolean isTimePassed(final long time, final long bigTime){
		return System.currentTimeMillis() - time < bigTime;
	}
	
	ServerMessage tryToReserveBook(ClientInterface cli, long bookId){
		if(clientDeception.containsKey(cli)){
			if(isTimePassed(clientDeception.get(cli),Config.TIME_PENALIZATION)){
				return ServerMessage.PENALISATION_ON;
			} 
			this.clientDeception.remove(cli);
		}
		
		if(this.booksReserved.containsKey(bookId)){
			
			if(!this.booksReserved.get(bookId).initTime.isPresent() && !isTimePassed(this.booksReserved.get(bookId).initTime.get(), Config.TIME_RESERVADO)){
				return ServerMessage.ALREADY_RESERVED;
			}
			this.booksReserved.remove(bookId);
			this.booksReserved.put(bookId, new A(cli, Optional.empty()));
		}
				
		this.booksReserved.put(bookId, new A(cli, Optional.empty()));
		System.out.println("Book "+ bookId + " reserved to "+ cli); 
		return ServerMessage.OPERATION_SUCESSFULL;
	}
	
	Optional<ClientInterface> giveBackBook(long bookId){
		if(!this.booksReserved.containsKey(bookId))
			return Optional.empty();
		
		if(!this.booksReserved.get(bookId).initTime.isPresent()){
			this.booksReserved.put(bookId, new A(this.booksReserved.get(bookId).c, Optional.of(System.currentTimeMillis())));
			return Optional.of(this.booksReserved.get(bookId).c);
		}	
		return Optional.empty();			
	}
	
	ServerMessage canClientBorrowBook(ClientInterface cli, long bookId){
		if(this.clientDeception.containsKey(cli)){
			if(isTimePassed(clientDeception.get(cli),Config.TIME_PENALIZATION)){
				return ServerMessage.PENALISATION_ON;
			} 
			this.clientDeception.remove(cli);
		}
		
		if(this.booksReserved.containsKey(bookId)){
			if(this.booksReserved.get(bookId).c.equals(cli))
				return ServerMessage.OPERATION_SUCESSFULL;
			else return ServerMessage.ALREADY_RESERVED;
		}
		return ServerMessage.OPERATION_SUCESSFULL;
	}		
}

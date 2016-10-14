package project02;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.swing.text.html.MinimalHTMLWriter;


class ClientHasBooks{
	final ClientInterface owner;
	final ArrayList<Long []> books;
	
	public ClientHasBooks( ClientInterface owner){
		this.owner = owner;
		this.books = new ArrayList<Long []>(3);
	}
	
	public boolean hasBook(int id){
		return books.stream().anyMatch(x -> x[0] == id);
	}
	
	public int howManyBooks(){
		return this.books.size();
	}
	
	/**
	 * The client rent the book with the id
	 * @param id
	 */
	public void addBook(long id){
		Long [] b = new Long[2]; 
		b[0] = id;
		b[1] = System.currentTimeMillis();
		books.add(b);
	}
	public boolean isGiveBackTimeOver(){
		for (Long [] book : this.books){
			if (isTimePassed(book[1], Config.TIME_EMPRESTADO))
				return true;
		}
		return false;
	}
	private boolean isTimePassed(final long time, final long bigTime){
		return System.currentTimeMillis() - time > bigTime;
	}
	/**
	 * Removes the book from the client and return the time that the book was rented;
	 * @param id
	 * @return
	 */
	public long removeBook(long id){
		Long [] elem = this.books.stream().filter(x -> x[0] == id).findFirst().get();
		books.remove(elem);
		return elem[1];
	}
}


public class Library {
	
	
	private final Map<Book, ClientHasBooks> bookList;
	private final ArrayList<ClientHasBooks> clientsBooksList;
	
	
	public Library(){
		this.bookList = new HashMap<Book, ClientHasBooks>();
		this.clientsBooksList = new ArrayList<ClientHasBooks>();
		loadBooksLibrary();
		
	}
	
	public ArrayList<Book> getBookList(){
		Book [] books = (Book[]) (this.bookList.entrySet().stream().filter(x -> true).toArray());
		return (ArrayList<Book>) Arrays.asList(books);
	}
	
	/**
	 * Load books from File
	 */
	private void loadBooksLibrary(){
		this.bookList.clear();
		
		try {
			for (String line : Files.readAllLines(Paths.get("books"))) {
				String args[] = line.split("+");
				this.bookList.put(new Book(Integer.parseInt(args[0]), args[1]), null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addBook(Book book){
		this.bookList.put(book, null);
	}
	
	public boolean isBookAvaliable(int bookId){
		try{
			return this.bookList.entrySet().stream().filter(x -> x.getKey().id == bookId).findFirst().get().getValue() == null;
		}catch(Exception e){
			System.out.println("Book is not registered in the library: "+bookId);
			return false;
		}
	}
	
	
	
	/**
	 * Classe assume que o livro nﾃ｣o estﾃ｡ reservado e o cliente nﾃ｣o estﾃ｡ penalizado
	 * @param client
	 * @param bookId
	 * @return
	 */
	public ServerMessage rentBook(ClientInterface client, long bookId){
			try{
				Entry<Book, ClientHasBooks> elem = this.bookList.entrySet().stream().filter(x -> x.getKey().id == bookId).findFirst().get();
				if(elem.getValue() == null){
					Optional<ClientHasBooks> e = this.clientsBooksList.stream().filter(x -> x.owner == client).findFirst();
					if( e.isPresent() ){
						if(e.get().howManyBooks() > Config.MAX_BOOKS){
							return ServerMessage.MAX_BOOK_REACHED;
						}
						if(e.get().isGiveBackTimeOver()){
							return ServerMessage.GIVE_BACK_BOOKS;
						}
						e.get().addBook(bookId);
						elem.setValue(e.get());
					}else{
						ClientHasBooks clientHasBooks = new ClientHasBooks(client);
						clientHasBooks.addBook(bookId);
						this.clientsBooksList.add(clientHasBooks);
						elem.setValue(clientHasBooks);
					}
					//TODO Implementar método de renovação
					return ServerMessage.OPERATION_SUCESSFULL;
				}else if(elem.getValue().owner == client){
					//AQUI ELE RENOVA
					Optional<ClientHasBooks> e = this.clientsBooksList.stream().filter(x -> x.owner == client).findFirst();					
					if(e.get().isGiveBackTimeOver()){
						return ServerMessage.GIVE_BACK_BOOKS;
					}
					elem.getValue().removeBook(bookId);
					ClientHasBooks clientHasBooks = new ClientHasBooks(client);
					clientHasBooks.addBook(bookId);
					this.clientsBooksList.add(clientHasBooks);
					elem.setValue(clientHasBooks);
					return ServerMessage.OPERATION_SUCESSFULL;
				}				
				else{
					//livro jﾃ｡ estﾃ｡ rented
					return ServerMessage.ALREADY_BOOKED;
				}
			}catch (Exception e) {
				return ServerMessage.BOOK_DONT_EXIST;
				// TODO: handle exception
			}
	}
	
	public long giveBackBooks(long bookId){
		long penalty=0;
		Entry<Book, ClientHasBooks> elem = this.bookList.entrySet().stream().filter(x -> x.getKey().id == bookId).findFirst().get();
		if(elem.getValue() == null){
			//Não está emprestado
		}
		else{
//			Optional<ClientHasBooks> e = this.clientsBooksList.stream().filter(x -> x == elem.getValue().owner).findFirst();
			long e=0;
			for(int i = 0; i < elem.getValue().books.size(); i++){
			       if(  elem.getValue().books.get(i)[0] == bookId){
			               e = elem.getValue().books.get(i)[1];
			               if(System.currentTimeMillis() - e > Config.TIME_EMPRESTADO){
			            	   penalty=((System.currentTimeMillis()-e)/Config.TIME_EMPRESTADO)*Config.TIME_PENALIZATION;
			               }
			       }
			}
			elem.getValue().removeBook(bookId);
			
		}
		return penalty;
	}
}

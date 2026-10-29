package ProtocoleBSPP.Reponse;

import model.entity.Book;
import protocol.Reponse;

import java.util.ArrayList;

public class ReponseSELECT_BOOK implements Reponse {
    private static final long serialVersionUID = 1L;
    private ArrayList<Book> listBook;

    public ReponseSELECT_BOOK() {
        listBook = new ArrayList<>();
    }
    public void addBook(Book b)
    {
        listBook.add(b);
    }
    public ArrayList<Book> getListBook()
    {
        return listBook;
    }
}

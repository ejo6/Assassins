package assassins;

import java.util.ArrayList;

public class GameNode<T> {
    private T data;
    private GameNode<T> next;
    private GameNode<T> prev;

    public GameNode(T data) {
        this.data = data;
        this.next = null;
        this.prev = null;
    }

    public T getData() {
        return data;
    }

    public GameNode<T> getNext() {
        return next;
    }

    public GameNode<T> getPrev() {
        return prev;
    } 

    public void setData(T newData) {
        this.data = newData;
    }

    public void setNext(GameNode<T> nextNode) {
        this.next = nextNode;
    }

    public void setPrev(GameNode<T> prevNode) {
        this.prev = prevNode;
    }

    public ArrayList<T> linkedToArrayList() {

        GameNode<T> curr = this;
        ArrayList<T> result = new ArrayList<>();

        while(curr != null) {
            result.add(curr.getData());
        }

        return result;
    }

    // Convert to arrayList for circular linked list (cycle graph)
    public ArrayList<T> linkedToArrayList(int numElements) {
        if (numElements == 0) {return null;}

        GameNode<T> curr = this;
        ArrayList<T> result = new ArrayList<>();

        int i = 0;
        while (curr != null && i < numElements) {
            result.add(curr.getData());
            curr = curr.getNext();
            i++;
        }

        return result;
    }
}

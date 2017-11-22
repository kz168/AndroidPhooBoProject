package phobooproject.com.zawad.phoboo.RequestUtils;

import android.content.Context;

import com.android.volley.Request;

import java.util.ArrayList;

public class CommandExec<T> {

    private ArrayList<Request<T>> requestList = new ArrayList<>();

    private Context context;

    public CommandExec(Context context){
        this.context = context;
    }

    public void add(Request<T> request){
        requestList.add(request);
    }

    public void remove(Request<T> request){
        requestList.remove(request);
    }

    public void execute(){
        for(Request<T> request: requestList){
            RequestSingleton.getInstance(context).addToRequestQueue(request);
        }
    }
}

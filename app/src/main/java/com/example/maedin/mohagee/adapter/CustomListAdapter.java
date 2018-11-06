package com.example.maedin.mohagee.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.maedin.mohagee.R;
import com.example.maedin.mohagee.item.PlaceItem;

import java.util.ArrayList;

public class CustomListAdapter extends BaseAdapter {

    //넣어줄 데이터 리스트
    private ArrayList<PlaceItem> placeList = null;
    private int listCnt = 0;

    LayoutInflater inflater = null;

    //생성자 : 데이터 셋팅
    public CustomListAdapter(ArrayList<PlaceItem> placeList) {
        this.placeList = placeList;
        listCnt = placeList.size();
    }

    //화면 갱신 전 호출, 아이템 갯수 결정
    @Override
    public int getCount() {
        return listCnt;
    }


    //리스트 뷰에 데이터를 넣어줌 - 화면 표시, position: 몇번째아이템
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //postion: List View의 위치
        //첫번째면 position = 0;
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null)
        {
            if (inflater == null)
                inflater = (LayoutInflater)  context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_custom, parent, false);
        }

        //위젯과 연결
        TextView name = (TextView) convertView.findViewById(R.id.list_custom_name);
        TextView category = (TextView) convertView.findViewById(R.id.list_custom_category);
        TextView theme = (TextView) convertView.findViewById(R.id.list_custom_theme);

        //"[ "+listVO.get(position).+" ] "
        //아이템 내 각 위젯에 데이터 반영

        PlaceItem temp = placeList.get(position);


        name.setText(temp.getName());
        category.setText(temp.getCategory());
        theme.setText(temp.getTheme());

        //아이템 클릭시
//        convertView.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v) {
//                ((MainActivity)v.getContext()).replaceDetail(listVO.get(pos));
//            }
//        });

        convertView.setTag(""+position);

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return placeList.get(position);
    }

}

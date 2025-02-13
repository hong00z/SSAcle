package com.example.firstproject.ui.home.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.R

class InboxListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InboxAdapter

    private val inboxList = listOf(
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요."),
        InboxItem(R.drawable.default_profile, "12기", "구미", "김보라 님께서 스터디 가입을 신청했어요.")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_notification_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = InboxAdapter(inboxList)
        recyclerView.adapter = adapter
        return view
    }
}

package com.academicpulse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.academicpulse.database.entity.Student;

import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> {

    private List<Student> participants;

    public ParticipantsAdapter(List<Student> participants) {
        this.participants = participants;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final TextView id;
        public final TextView status;
        public final ImageView avatar;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tv_participant_name);
            id = view.findViewById(R.id.tv_participant_id);
            status = view.findViewById(R.id.tv_participant_status);
            avatar = view.findViewById(R.id.iv_participant_avatar);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = participants.get(position);
        holder.name.setText(student.getName());
        holder.id.setText(String.format("ID: %s", student.getId()));
        holder.status.setText("admin".equals(student.getRole()) ? "Administrator" : "បានចុះឈ្មោះ");
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public void updateParticipants(List<Student> newParticipants) {
        this.participants = newParticipants;
        notifyDataSetChanged();
    }
}

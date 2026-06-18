package com.academicpulse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.academicpulse.database.entity.Student;

import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> {

    private List<Student> participants;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Student student);
    }

    public ParticipantsAdapter(List<Student> participants) {
        this.participants = participants;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final TextView id;
        public final TextView status;
        public final ImageView avatar;
        public final ImageButton btnDelete;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tv_participant_name);
            id = view.findViewById(R.id.tv_participant_id);
            status = view.findViewById(R.id.tv_participant_status);
            avatar = view.findViewById(R.id.iv_participant_avatar);
            btnDelete = view.findViewById(R.id.btn_delete_participant);
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

        // Only show delete button if listener is set (usually only in Admin's ParticipantsFragment)
        holder.btnDelete.setVisibility(deleteListener != null ? View.VISIBLE : View.GONE);

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(student);
            }
        });
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

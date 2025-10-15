package models

type Complaint struct {
	ComplaintID string `json:"complaintId"`
	UserID      string `json:"userId"`
	Description string `json:"description"`
	Status      string `json:"status"`
	Category    string `json:"category"`
	CreatedAt   string `json:"createdAt"`
}

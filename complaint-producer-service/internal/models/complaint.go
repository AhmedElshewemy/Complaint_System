package models

type Complaint struct {
	ID          string `json:"id"`
	User        string `json:"user"`
	Description string `json:"description"`
	Status      string `json:"status"`
	Category    string `json:"category"`
	CreatedAt   string `json:"createdAt"`
}

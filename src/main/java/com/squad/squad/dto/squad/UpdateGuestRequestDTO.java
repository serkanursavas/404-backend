package com.squad.squad.dto.squad;

/** Üyenin oyuncu kaydını "misafir" yapar/geri alır: misafir oyuncu sıralamalarda ve ekip listelerinde görünmez. */
public class UpdateGuestRequestDTO {
    private Boolean guest;

    public Boolean getGuest() {
        return guest;
    }

    public void setGuest(Boolean guest) {
        this.guest = guest;
    }
}

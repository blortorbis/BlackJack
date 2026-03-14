import tkinter as tk
import random

# Card and Deck logic
suits = ['Hearts', 'Diamonds', 'Clubs', 'Spades']
ranks = ['2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K', 'A']
values = {'2': 2, '3': 3, '4': 4, '5': 5, '6': 6, '7': 7, '8': 8, '9': 9, '10': 10, 'J': 10, 'Q': 10, 'K': 10, 'A': 11}

class Card:
    def __init__(self, suit, rank):
        self.suit = suit
        self.rank = rank
        self.value = values[rank]
    def __str__(self):
        return f'{self.rank} of {self.suit}'

class Deck:
    def __init__(self):
        self.cards = [Card(suit, rank) for suit in suits for rank in ranks]
        random.shuffle(self.cards)
    def deal(self):
        return self.cards.pop()

# Game logic
class Hand:
    def __init__(self):
        self.cards = []
    def add_card(self, card):
        self.cards.append(card)
    def get_value(self):
        value = sum(card.value for card in self.cards)
        # Adjust for Aces
        aces = sum(1 for card in self.cards if card.rank == 'A')
        while value > 21 and aces:
            value -= 10
            aces -= 1
        return value
    def __str__(self):
        return ', '.join(str(card) for card in self.cards)

class BlackjackGame:
    def __init__(self, root):
        self.root = root
        self.root.title('Blackjack')
        self.deck = Deck()
        self.player_hand = Hand()
        self.dealer_hand = Hand()
        self.setup_ui()
        self.start_game()

    def setup_ui(self):
        self.player_label = tk.Label(self.root, text='Player Hand:')
        self.player_label.pack()
        self.player_cards = tk.Label(self.root, text='')
        self.player_cards.pack()
        self.dealer_label = tk.Label(self.root, text='Dealer Hand:')
        self.dealer_label.pack()
        self.dealer_cards = tk.Label(self.root, text='')
        self.dealer_cards.pack()
        self.status = tk.Label(self.root, text='')
        self.status.pack()
        self.hit_button = tk.Button(self.root, text='Hit', command=self.hit)
        self.hit_button.pack(side='left')
        self.stand_button = tk.Button(self.root, text='Stand', command=self.stand)
        self.stand_button.pack(side='left')
        self.restart_button = tk.Button(self.root, text='Restart', command=self.start_game)
        self.restart_button.pack(side='left')

    def start_game(self):
        self.deck = Deck()
        self.player_hand = Hand()
        self.dealer_hand = Hand()
        self.status.config(text='')
        self.player_hand.add_card(self.deck.deal())
        self.player_hand.add_card(self.deck.deal())
        self.dealer_hand.add_card(self.deck.deal())
        self.dealer_hand.add_card(self.deck.deal())
        self.update_ui()
        self.hit_button.config(state='normal')
        self.stand_button.config(state='normal')

    def update_ui(self):
        self.player_cards.config(text=f'{self.player_hand} (Value: {self.player_hand.get_value()})')
        self.dealer_cards.config(text=f'{self.dealer_hand.cards[0]} and [Hidden]')

    def hit(self):
        self.player_hand.add_card(self.deck.deal())
        self.update_ui()
        if self.player_hand.get_value() > 21:
            self.status.config(text='Player busts! Dealer wins.')
            self.hit_button.config(state='disabled')
            self.stand_button.config(state='disabled')

    def stand(self):
        self.hit_button.config(state='disabled')
        self.stand_button.config(state='disabled')
        # Reveal dealer hand
        while self.dealer_hand.get_value() < 17:
            self.dealer_hand.add_card(self.deck.deal())
        self.dealer_cards.config(text=f'{self.dealer_hand} (Value: {self.dealer_hand.get_value()})')
        player_value = self.player_hand.get_value()
        dealer_value = self.dealer_hand.get_value()
        if dealer_value > 21:
            self.status.config(text='Dealer busts! Player wins.')
        elif dealer_value > player_value:
            self.status.config(text='Dealer wins.')
        elif dealer_value < player_value:
            self.status.config(text='Player wins!')
        else:
            self.status.config(text='Push (Tie).')

if __name__ == '__main__':
    root = tk.Tk()
    game = BlackjackGame(root)
    root.mainloop()

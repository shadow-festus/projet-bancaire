export interface Client {
  clientId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  address: string;
  dateOfBirth: string;
  accountIds: string[];
}

export interface Account {
  accountId: string;
  clientId: string;
  accountType: 'checking' | 'savings' | 'investment' | 'credit';
  openDate: string;
  balance: number;
  currency: string;
  status: 'active' | 'inactive' | 'closed' | 'suspended';
}

export interface Transaction {
  transactionId: string;
  accountId: string;
  type: 'deposit' | 'withdrawal' | 'transfer' | 'payment' | 'fee';
  amount: number;
  date: string;
  description: string;
  currency: string;
  balanceAfter: number;
}

export const mockClients: Client[] = [
  {
    clientId: 'C1001',
    firstName: 'Sarah',
    lastName: 'Johnson',
    email: 'sarah.j@example.com',
    phoneNumber: '+1 (555) 123-4567',
    address: '742 Evergreen Terrace, Springfield',
    dateOfBirth: '1985-05-15',
    accountIds: ['ACC-001', 'ACC-002'],
  },
  {
    clientId: 'C1002',
    firstName: 'Michael',
    lastName: 'Chen',
    email: 'm.chen@example.com',
    phoneNumber: '+1 (555) 987-6543',
    address: '123 Maple Avenue, Riverdale',
    dateOfBirth: '1992-11-28',
    accountIds: ['ACC-003'],
  },
  {
    clientId: 'C1003',
    firstName: 'Elena',
    lastName: 'Rodriguez',
    email: 'elena.rod@example.com',
    phoneNumber: '+1 (555) 456-7890',
    address: '456 Oak Street, Metropolis',
    dateOfBirth: '1978-03-10',
    accountIds: ['ACC-004', 'ACC-005'],
  },
];

export const mockAccounts: Account[] = [
  {
    accountId: 'ACC-001',
    clientId: 'C1001',
    accountType: 'checking',
    openDate: '2020-01-12',
    balance: 4250.75,
    currency: 'USD',
    status: 'active',
  },
  {
    accountId: 'ACC-002',
    clientId: 'C1001',
    accountType: 'savings',
    openDate: '2020-01-12',
    balance: 15700.0,
    currency: 'USD',
    status: 'active',
  },
  {
    accountId: 'ACC-003',
    clientId: 'C1002',
    accountType: 'investment',
    openDate: '2021-06-20',
    balance: 8900.25,
    currency: 'USD',
    status: 'active',
  },
  {
    accountId: 'ACC-004',
    clientId: 'C1003',
    accountType: 'checking',
    openDate: '2019-03-15',
    balance: 1200.5,
    currency: 'USD',
    status: 'active',
  },
  {
    accountId: 'ACC-005',
    clientId: 'C1003',
    accountType: 'credit',
    openDate: '2019-11-02',
    balance: -450.0,
    currency: 'USD',
    status: 'active',
  },
];

export const mockTransactions: Transaction[] = [
  {
    transactionId: 'TXN-7001',
    accountId: 'ACC-001',
    type: 'deposit',
    amount: 1500.0,
    date: '2024-03-20',
    description: 'Monthly Salary Deposit',
    currency: 'USD',
    balanceAfter: 4250.75,
  },
  {
    transactionId: 'TXN-7002',
    accountId: 'ACC-001',
    type: 'withdrawal',
    amount: 120.5,
    date: '2024-03-21',
    description: 'ATM Cash Withdrawal',
    currency: 'USD',
    balanceAfter: 4130.25,
  },
  {
    transactionId: 'TXN-7003',
    accountId: 'ACC-005',
    type: 'payment',
    amount: 50.0,
    date: '2024-03-22',
    description: 'Supermarket Purchase',
    currency: 'USD',
    balanceAfter: -450.0,
  },
  {
    transactionId: 'TXN-7004',
    accountId: 'ACC-002',
    type: 'transfer',
    amount: 200.0,
    date: '2024-03-23',
    description: 'Transfer to Savings',
    currency: 'USD',
    balanceAfter: 15700.0,
  },
];

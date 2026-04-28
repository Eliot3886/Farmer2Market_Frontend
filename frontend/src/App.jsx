import React, { useState, useEffect } from 'react';
import { 
  ShoppingBasket, 
  User, 
  MessageSquare, 
  Plus, 
  Search, 
  ArrowRight, 
  LogOut, 
  Mic, 
  Camera,
  MapPin,
  Phone,
  Settings,
  Package,
  Layers,
  ChevronRight
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

// --- COMPONENTS ---

// 1. Role Selection Screen
const RoleSelection = ({ onSelect }) => (
  <div className="premium-container" style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
    <motion.div 
      initial={{ opacity: 0, scale: 0.9 }}
      animate={{ opacity: 1, scale: 1 }}
      className="glass-card" 
      style={{ maxWidth: '500px', width: '100%', textAlign: 'center' }}
    >
      <h1 style={{ fontSize: '2.5rem', marginBottom: '8px', color: 'var(--accent)' }}>Nyatanga Farm</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '40px' }}>Select your role to continue to the marketplace</p>
      
      <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
        <button className="btn-primary" style={{ padding: '24px' }} onClick={() => onSelect('Farmer')}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ background: 'rgba(255,255,255,0.1)', padding: '12px', borderRadius: '12px' }}>
              <Package size={32} />
            </div>
            <div style={{ textAlign: 'left' }}>
              <div style={{ fontWeight: 700, fontSize: '1.2rem' }}>I am a Farmer</div>
              <div style={{ fontWeight: 400, opacity: 0.8, fontSize: '0.9rem' }}>Sell and manage your produce</div>
            </div>
          </div>
        </button>

        <button className="btn-outline" style={{ padding: '24px' }} onClick={() => onSelect('Buyer')}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ background: 'rgba(45,90,39,0.1)', padding: '12px', borderRadius: '12px' }}>
              <ShoppingBasket size={32} />
            </div>
            <div style={{ textAlign: 'left' }}>
              <div style={{ fontWeight: 700, fontSize: '1.2rem' }}>I am a Buyer</div>
              <div style={{ fontWeight: 400, opacity: 0.8, fontSize: '0.9rem' }}>Purchase farm-fresh goods</div>
            </div>
          </div>
        </button>
      </div>
    </motion.div>
  </div>
);

// 2. Buyer Entry Screen
const BuyerEntry = ({ onEnter }) => {
  const [fid, setFid] = useState('');
  return (
    <div className="premium-container" style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <motion.div 
        initial={{ opacity: 0, x: 20 }}
        animate={{ opacity: 1, x: 0 }}
        className="glass-card" 
        style={{ maxWidth: '400px', width: '100%' }}
      >
        <h2>Farmer ID Access</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '24px' }}>Enter the ID of the farmer you want to visit</p>
        <div style={{ position: 'relative', marginBottom: '20px' }}>
          <input 
            type="text" 
            placeholder="e.g. FARM-A1B2C3" 
            value={fid}
            onChange={(e) => setFid(e.target.value.toUpperCase())}
            style={{ 
              width: '100%', 
              background: 'rgba(255,255,255,0.05)', 
              border: '1px solid var(--glass-border)', 
              borderRadius: '12px', 
              padding: '14px 16px', 
              color: 'white',
              fontSize: '1rem'
            }} 
          />
        </div>
        <button className="btn-primary" style={{ width: '100%' }} onClick={() => onEnter(fid)}>
          Enter Dashboard <ArrowRight size={20} />
        </button>
      </motion.div>
    </div>
  );
};

// 3. Farmer Login/Register (Simplified for demo, but professional)
const FarmerAuth = ({ onAuth }) => {
  const [isLogin, setIsLogin] = useState(true);
  return (
    <div className="premium-container" style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="glass-card" 
        style={{ maxWidth: '450px', width: '100%' }}
      >
        <h2 style={{ marginBottom: '8px' }}>{isLogin ? 'Farmer Login' : 'Farmer Registration'}</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '24px' }}>Access your farm management tools</p>
        
        <form style={{ display: 'flex', flexDirection: 'column', gap: '16px' }} onSubmit={(e) => { e.preventDefault(); onAuth(); }}>
          {!isLogin && (
            <input type="text" placeholder="Full Name" style={inputStyle} required />
          )}
          <input type="email" placeholder="Email Address" style={inputStyle} required />
          <input type="password" placeholder="Password" style={inputStyle} required />
          
          <button type="submit" className="btn-primary" style={{ marginTop: '12px' }}>
            {isLogin ? 'Login to Dashboard' : 'Create Account'}
          </button>
        </form>
        
        <p style={{ marginTop: '20px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          {isLogin ? "Don't have an account?" : "Already have an account?"} 
          <span 
            style={{ color: 'var(--accent)', cursor: 'pointer', marginLeft: '6px', fontWeight: 600 }}
            onClick={() => setIsLogin(!isLogin)}
          >
            {isLogin ? 'Register Now' : 'Login'}
          </span>
        </p>
      </motion.div>
    </div>
  );
};

const inputStyle = {
  width: '100%', 
  background: 'rgba(255,255,255,0.05)', 
  border: '1px solid var(--glass-border)', 
  borderRadius: '12px', 
  padding: '14px 16px', 
  color: 'white',
  fontSize: '1rem'
};

// --- MAIN DASHBOARD (Unified for Farmer/Buyer with role-based features) ---

const Dashboard = ({ role, userData, farmerId, onLogout }) => {
  const [activeTab, setActiveTab] = useState('inventory');
  
  return (
    <div className="dashboard-root">
      {/* Sticky Header */}
      <header className="sticky-header">
        <div className="premium-container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ background: 'var(--primary)', padding: '8px', borderRadius: '10px' }}>
              <Package color="white" size={24} />
            </div>
            <h2 style={{ fontWeight: 700, fontSize: '1.4rem' }}>NYATANGA <span style={{ color: 'var(--accent)' }}>FARMS</span></h2>
          </div>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ textAlign: 'right', marginRight: '8px' }}>
              <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{userData.name}</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{role}: {farmerId}</div>
            </div>
            <button 
              className="glass-card" 
              style={{ padding: '8px', borderRadius: '10px', border: 'none', cursor: 'pointer', color: 'var(--error)' }}
              onClick={onLogout}
            >
              <LogOut size={20} />
            </button>
          </div>
        </div>
        
        <nav className="nav-links">
          <div className={`nav-link ${activeTab === 'inventory' ? 'active' : ''}`} onClick={() => setActiveTab('inventory')}>Inventory</div>
          <div className={`nav-link ${activeTab === 'catalogue' ? 'active' : ''}`} onClick={() => setActiveTab('catalogue')}>Produce Catalogue</div>
          <div className={`nav-link ${activeTab === 'chat' ? 'active' : ''}`} onClick={() => setActiveTab('chat')}>Messages</div>
          <div className={`nav-link ${activeTab === 'profile' ? 'active' : ''}`} onClick={() => setActiveTab('profile')}>Profile</div>
        </nav>
      </header>

      <main className="premium-container" style={{ padding: '40px 0' }}>
        <AnimatePresence mode="wait">
          {activeTab === 'inventory' && <InventoryView key="inventory" role={role} />}
          {activeTab === 'catalogue' && <CatalogueView key="catalogue" role={role} />}
          {activeTab === 'chat' && <ChatView key="chat" role={role} />}
          {activeTab === 'profile' && <ProfileView key="profile" role={role} userData={userData} farmerId={farmerId} />}
        </AnimatePresence>
      </main>
    </div>
  );
};

// --- SUB-VIEWS ---

const InventoryView = ({ role }) => (
  <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -10 }}>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
      <h1>Farm Inventory</h1>
      {role === 'Farmer' && (
        <button className="btn-primary"><Plus size={20} /> Add Item</button>
      )}
    </div>
    <div className="grid-3">
      {[1, 2, 3, 4].map(i => (
        <div key={i} className="glass-card" style={{ padding: '0', overflow: 'hidden' }}>
          <div style={{ height: '180px', background: 'rgba(255,255,255,0.05)', position: 'relative' }}>
             <img src={`https://images.unsplash.com/photo-1595855759920-86582396756a?q=80&w=400&h=200&auto=format&fit=crop`} style={{ width: '100%', height: '100%', objectFit: 'cover' }} alt="Produce" />
             <div style={{ position: 'absolute', top: '12px', right: '12px', background: 'var(--primary)', color: 'white', padding: '4px 10px', borderRadius: '20px', fontSize: '0.8rem' }}>
               $5.00 / kg
             </div>
          </div>
          <div style={{ padding: '20px' }}>
            <h3 style={{ marginBottom: '8px' }}>Organic Potatoes</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '16px' }}>Freshly harvested from the northern valley fields. High starch content, perfect for baking.</p>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
               <span style={{ fontSize: '0.8rem', color: 'var(--accent)' }}>500kg Available</span>
               {role === 'Buyer' ? <button className="btn-primary" style={{ padding: '8px 16px', fontSize: '0.8rem' }}>Order Now</button> : <button className="btn-outline" style={{ padding: '8px 16px', fontSize: '0.8rem' }}>Edit</button>}
            </div>
          </div>
        </div>
      ))}
    </div>
  </motion.div>
);

const CatalogueView = ({ role }) => (
  <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -10 }}>
    <div style={{ marginBottom: '32px' }}>
      <h1>Produce Catalogue</h1>
      <p style={{ color: 'var(--text-muted)' }}>Browse all seasonal offerings</p>
    </div>
    <div style={{ display: 'flex', gap: '12px', marginBottom: '32px', overflowX: 'auto', paddingBottom: '12px' }}>
       {['All', 'Vegetables', 'Fruits', 'Grains', 'Dairy', 'Livestock'].map(cat => (
         <button key={cat} style={{ whiteSpace: 'nowrap', padding: '10px 24px', borderRadius: '30px', background: cat === 'All' ? 'var(--primary)' : 'var(--glass)', border: '1px solid var(--glass-border)', color: 'white', cursor: 'pointer' }}>{cat}</button>
       ))}
    </div>
    <div className="grid-3">
       {[1, 2, 3, 4, 5, 6].map(i => (
         <div key={i} className="glass-card" style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
            <div style={{ width: '80px', height: '80px', borderRadius: '16px', background: 'rgba(255,255,255,0.05)', overflow: 'hidden' }}>
              <img src={`https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?q=80&w=100&h=100&auto=format&fit=crop`} alt="item" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            </div>
            <div style={{ flex: 1 }}>
               <h4 style={{ marginBottom: '4px' }}>Premium Carrots</h4>
               <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>In Stock: 200kg</p>
               <div style={{ color: 'var(--accent)', fontWeight: 600, fontSize: '0.9rem' }}>$1.20 / bundle</div>
            </div>
            <ChevronRight size={20} color="var(--text-muted)" />
         </div>
       ))}
    </div>
  </motion.div>
);

const ProfileView = ({ role, userData, farmerId }) => (
  <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -10 }}>
    <div className="glass-card" style={{ padding: '0', overflow: 'hidden' }}>
       <div className="profile-header">
         <div className="profile-avatar">
            <User size={48} />
         </div>
       </div>
       <div style={{ padding: '80px 40px 40px 40px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '40px' }}>
            <div>
               <h1 style={{ marginBottom: '4px' }}>{userData.name}</h1>
               <p style={{ color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '8px' }}>
                 <MapPin size={16} /> Nyatanga Village, Field 42
               </p>
            </div>
            <button className="btn-outline">Edit Profile</button>
          </div>
          
          <div className="grid-3" style={{ gap: '20px' }}>
             <div className="glass-card" style={{ padding: '20px' }}>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginBottom: '4px' }}>Farmer Unique ID</div>
                <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--accent)' }}>{farmerId}</div>
             </div>
             <div className="glass-card" style={{ padding: '20px' }}>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginBottom: '4px' }}>Contact Number</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 600 }}>+263 771 234 567</div>
             </div>
             <div className="glass-card" style={{ padding: '20px' }}>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginBottom: '4px' }}>Account Status</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--success)' }}>Verified Producer</div>
             </div>
          </div>
          
          <div style={{ marginTop: '40px' }}>
             <h3 style={{ marginBottom: '20px' }}>Registration Details</h3>
             <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <DetailRow label="Farm Name" value="Nyatanga Green Valley" />
                <DetailRow label="Primary Produce" value="Vegetables & Grains" />
                <DetailRow label="Join Date" value="April 12, 2024" />
                <DetailRow label="Operational Since" value="2018" />
             </div>
          </div>
       </div>
    </div>
  </motion.div>
);

const DetailRow = ({ label, value }) => (
  <div style={{ display: 'flex', padding: '12px 0', borderBottom: '1px solid var(--glass-border)' }}>
    <div style={{ width: '200px', color: 'var(--text-muted)' }}>{label}</div>
    <div style={{ fontWeight: 500 }}>{value}</div>
  </div>
);

const ChatView = ({ role }) => {
  const [msg, setMsg] = useState('');
  const [messages, setMessages] = useState([
    { id: 1, text: "Hello! Is the organic kale available?", type: 'received' },
    { id: 2, text: "Yes, we just harvested a fresh batch this morning.", type: 'sent' },
    { id: 3, text: "Great, I'll take 5kg. Can I pick it up tomorrow?", type: 'received' },
    { id: 4, type: 'voice', type_class: 'sent' }, // Voice note example
  ]);

  return (
    <div className="chat-window">
      <div style={{ padding: '20px', borderBottom: '1px solid var(--glass-border)', display: 'flex', alignItems: 'center', gap: '12px', background: 'var(--glass)' }}>
         <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: 'var(--primary-light)' }}></div>
         <div>
            <div style={{ fontWeight: 600 }}>{role === 'Farmer' ? 'Tendai (Buyer)' : 'Nyatanga Farm'}</div>
            <div style={{ fontSize: '0.7rem', color: 'var(--success)' }}>Online</div>
         </div>
      </div>
      
      <div className="chat-messages">
        {messages.map((m) => (
          <div key={m.id} className={`message-bubble message-${m.type_class || m.type}`}>
            {m.type === 'voice' ? (
              <div className="voice-note">
                <Mic size={16} />
                <div style={{ flex: 1, height: '4px', background: 'rgba(255,255,255,0.2)', borderRadius: '2px' }}></div>
                <span style={{ fontSize: '0.7rem' }}>0:12</span>
              </div>
            ) : (
              m.text
            )}
            <div style={{ fontSize: '0.6rem', opacity: 0.5, marginTop: '4px', textAlign: 'right' }}>12:45 PM</div>
          </div>
        ))}
      </div>
      
      <div style={{ padding: '20px', borderTop: '1px solid var(--glass-border)', display: 'flex', gap: '12px' }}>
         <button className="glass-card" style={{ padding: '12px' }}><Camera size={20} /></button>
         <input 
           type="text" 
           placeholder="Type a message..." 
           value={msg}
           onChange={(e) => setMsg(e.target.value)}
           style={{ flex: 1, background: 'rgba(255,255,255,0.05)', border: 'none', borderRadius: '12px', padding: '0 16px', color: 'white' }} 
         />
         <button className="glass-card" style={{ padding: '12px', color: 'var(--accent)' }}><Mic size={20} /></button>
         <button className="btn-primary" style={{ padding: '0 24px' }}>Send</button>
      </div>
    </div>
  );
};

// --- APP ROOT ---

export default function App() {
  const [view, setView] = useState('role-select'); // role-select, farmer-auth, buyer-entry, dashboard
  const [role, setRole] = useState(null);
  const [farmerId, setFarmerId] = useState('');
  const [userData, setUserData] = useState({ name: 'Guest' });

  const handleRoleSelect = (selectedRole) => {
    setRole(selectedRole);
    if (selectedRole === 'Farmer') {
      setView('farmer-auth');
    } else {
      setView('buyer-entry');
    }
  };

  const handleFarmerAuth = () => {
    // Mock successful login/registration
    setUserData({ name: 'Farmer Nyatanga' });
    setFarmerId('FARM-XT92L0'); // Automatically generated in production
    setView('dashboard');
  };

  const handleBuyerEntry = (fid) => {
    if (!fid) return alert("Please enter a Farmer ID");
    setUserData({ name: 'Tendai M.' });
    setFarmerId(fid);
    setView('dashboard');
  };

  const handleLogout = () => {
    setView('role-select');
    setRole(null);
    setFarmerId('');
  };

  return (
    <div className="hero-gradient" style={{ minHeight: '100vh' }}>
      {view === 'role-select' && <RoleSelection onSelect={handleRoleSelect} />}
      {view === 'farmer-auth' && <FarmerAuth onAuth={handleFarmerAuth} />}
      {view === 'buyer-entry' && <BuyerEntry onEnter={handleBuyerEntry} />}
      {view === 'dashboard' && (
        <Dashboard 
          role={role} 
          userData={userData} 
          farmerId={farmerId} 
          onLogout={handleLogout} 
        />
      )}
    </div>
  );
}

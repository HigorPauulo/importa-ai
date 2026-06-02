type IconProps = { className?: string }

const base = {
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 1.75,
    strokeLinecap: 'round' as const,
    strokeLinejoin: 'round' as const,
}

export function IconDashboard({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <path d="M3 10.5 12 3l9 7.5" />
            <path d="M5 9.5V21h14V9.5" />
            <path d="M9.5 21v-6h5v6" />
        </svg>
    )
}

export function IconBox({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <path d="M21 7.5 12 12 3 7.5 12 3l9 4.5Z" />
            <path d="M3 7.5v9L12 21l9-4.5v-9" />
            <path d="M12 12v9" />
        </svg>
    )
}

export function IconPlus({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <path d="M12 5v14M5 12h14" />
        </svg>
    )
}

export function IconDollar({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <path d="M12 3v18" />
            <path d="M16.5 7.5c0-1.7-1.8-3-4.5-3s-4.5 1.3-4.5 3 1.8 3 4.5 3 4.5 1.3 4.5 3-1.8 3-4.5 3-4.5-1.3-4.5-3" />
        </svg>
    )
}

export function IconBell({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.7 21a2 2 0 0 1-3.4 0" />
        </svg>
    )
}

export function IconUsers({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>
    )
}

export function IconDownload({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
            <path d="M7 10l5 5 5-5" />
            <path d="M12 15V3" />
        </svg>
    )
}

export function IconMenu({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <path d="M4 6h16M4 12h16M4 18h16" />
        </svg>
    )
}

export function IconClose({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <path d="M6 6l12 12M18 6 6 18" />
        </svg>
    )
}

export function IconSearch({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <circle cx="11" cy="11" r="7" />
            <path d="m21 21-4.3-4.3" />
        </svg>
    )
}

export function IconStar({ className }: IconProps) {
    return (
        <svg viewBox="0 0 24 24" fill="currentColor" className={className} aria-hidden="true">
            <path d="M12 2.5l2.9 5.9 6.5.9-4.7 4.6 1.1 6.5L12 17.8 6.2 20.9l1.1-6.5L2.6 9.8l6.5-.9L12 2.5Z" />
        </svg>
    )
}

export function IconShare({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <circle cx="18" cy="5" r="3" />
            <circle cx="6" cy="12" r="3" />
            <circle cx="18" cy="19" r="3" />
            <path d="m8.6 13.5 6.8 4M15.4 6.5l-6.8 4" />
        </svg>
    )
}

export function IconLogout({ className }: IconProps) {
    return (
        <svg {...base} className={className} aria-hidden="true">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <path d="m16 17 5-5-5-5M21 12H9" />
        </svg>
    )
}
